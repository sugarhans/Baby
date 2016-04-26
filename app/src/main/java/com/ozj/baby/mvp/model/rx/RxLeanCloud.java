package com.ozj.baby.mvp.model.rx;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.orhanobut.logger.Logger;
import com.ozj.baby.di.scope.ContextLife;
import com.ozj.baby.mvp.model.bean.Gallery;
import com.ozj.baby.mvp.model.bean.Souvenir;
import com.ozj.baby.mvp.model.dao.GalleryDao;
import com.ozj.baby.mvp.model.dao.SouvenirDao;
import com.ozj.baby.mvp.model.dao.UserDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/4/20.
 */
public class RxLeanCloud {
    private static volatile RxLeanCloud mRxLeanCloud;
    private Context mContext;

    @Singleton
    @Inject
    public RxLeanCloud(@ContextLife("Application") Context context) {
        mContext = context;
    }


    public RxLeanCloud getInstance(Context context) {
        if (mRxLeanCloud == null) {
            synchronized (RxLeanCloud.class) {
                if (mRxLeanCloud == null) {
                    mRxLeanCloud = new RxLeanCloud(context);

                }
            }
        }
        return mRxLeanCloud;
    }

    public Observable<Souvenir> SaveSouvenirByLeanCloud(final Souvenir souvenir) {
        return Observable.create(new Observable.OnSubscribe<Souvenir>() {
            @Override
            public void call(final Subscriber<? super Souvenir> subscriber) {
                souvenir.setFetchWhenSave(true);
                souvenir.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(souvenir);
                        } else {
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();
                    }
                });
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> SaveUserByLeanCloud(final AVUser user) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(true);
                        } else {
                            subscriber.onError(e);
                            Logger.e(e.getMessage());
                        }
                        subscriber.onCompleted();
                    }
                });

            }
        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<AVUser> GetUserByLeanCloud(final String objectId) {

        return Observable.create(new Observable.OnSubscribe<AVUser>() {
            @Override
            public void call(Subscriber<? super AVUser> subscriber) {

                AVQuery<AVUser> query = AVUser.getQuery();

                try {
                    AVUser user = query.include(UserDao.AVATARURL).get(objectId);
                    subscriber.onNext(user);

                } catch (AVException e) {
                    e.printStackTrace();
                    subscriber.onError(e);

                }
                subscriber.onCompleted();


            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<AVUser> GetUserByUsername(final String username) {
        return Observable.create(new Observable.OnSubscribe<AVUser>() {
            @Override
            public void call(Subscriber<? super AVUser> subscriber) {
                AVQuery<AVUser> query = AVUser.getQuery();
                try {
                    AVUser user = query.whereEqualTo(UserDao.USERNAME, username).getFirst();
                    subscriber.onNext(user);
                } catch (AVException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<List<Souvenir>> GetALlSouvenirByLeanCloud(final String authorId, final String loverid, final int size, final int page) {
        return Observable.create(new Observable.OnSubscribe<List<Souvenir>>() {
            @Override
            public void call(final Subscriber<? super List<Souvenir>> subscriber) {

                AVQuery<Souvenir> query = AVQuery.getQuery(SouvenirDao.TABLENAME);
                query.whereEqualTo(SouvenirDao.SOUVENIR_AUTHORID, authorId);
                AVQuery<Souvenir> query1 = AVQuery.getQuery(SouvenirDao.TABLENAME);
                query1.whereEqualTo(SouvenirDao.SOUVENIR_AUTHORID, loverid);
                List<AVQuery<Souvenir>> queries = new ArrayList<>();
                queries.add(query);
                queries.add(query1);
                AVQuery<Souvenir> mainquery = AVQuery.or(queries);
                mainquery.setLimit(size);
                mainquery.setSkip(size * page);
                mainquery.include(SouvenirDao.SOUVENIR_AUTHOR);
                mainquery.orderByDescending("createdAt");
                mainquery.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
                mainquery.findInBackground(new FindCallback<Souvenir>() {
                    @Override
                    public void done(List<Souvenir> list, AVException e) {
                        if (e == null) {
                            subscriber.onNext(list);
                        } else {
                            subscriber.onError(e);
                            Logger.e(e.getMessage());
                        }
                        subscriber.onCompleted();

                    }
                });
            }
        }).subscribeOn(AndroidSchedulers.mainThread());

    }

    public Observable<AVFile> UploadFile(final AVFile file) {
        return Observable.create(new Observable.OnSubscribe<AVFile>() {
            @Override
            public void call(final Subscriber<? super AVFile> subscriber) {
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(file);
                        } else {
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();

                    }
                });
            }
        });

    }


    public Observable<String> UploadPicture(final AVFile avFile) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                avFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(avFile.getUrl());
                        } else {
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();
                    }
                });
            }
        }).subscribeOn(AndroidSchedulers.mainThread());

    }

    public Observable<List<Gallery>> FetchAllPicture(final String authorId, final String theotherone) {
        return Observable.create(new Observable.OnSubscribe<List<Gallery>>() {
            @Override
            public void call(final Subscriber<? super List<Gallery>> subscriber) {
                AVQuery<Gallery> query = AVQuery.getQuery(Gallery.class);
                query.whereEqualTo(GalleryDao.AUTHORID, authorId);
                AVQuery<Gallery> query1 = AVQuery.getQuery(Gallery.class);
                query1.whereEqualTo(GalleryDao.AUTHORID, theotherone);
                List<AVQuery<Gallery>> queries = new ArrayList<>();
                queries.add(query);
                queries.add(query1);
                AVQuery<Gallery> mainquery = AVQuery.or(queries);
                mainquery.orderByDescending("createdAt");
                mainquery.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
                mainquery.findInBackground(new FindCallback<Gallery>() {

                    @Override
                    public void done(List<Gallery> list, AVException e) {
                        if (e == null) {
                            subscriber.onNext(list);

                        } else {
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();

                    }
                });

            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }


    public Observable<Gallery> saveGallery(final Gallery gallery) {
        return Observable.create(new Observable.OnSubscribe<Gallery>() {
            @Override
            public void call(final Subscriber<? super Gallery> subscriber) {
                gallery.setFetchWhenSave(true);
                gallery.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(gallery);
                        } else {
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();
                    }
                });


            }
        }).subscribeOn(Schedulers.io());
    }

}
