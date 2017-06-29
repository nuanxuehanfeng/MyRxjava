package com.bawei.wangxueshi.myrxjava;

import android.app.Activity;
import android.os.Bundle;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        just();
    }
    Disposable disposable;//通过这个对象取消 被观察者和观察者的关联
    //分开写观察者和被观察者，并进行简单说明；
    public void text01(){
//观察者和被观察者 默认是在主线程的
//        1．被观察者可以发送⽆限个onNext, 观察者也可以接收⽆限个onNext.
//        2．当Observable发送了⼀个onComplete后, Observable的onComplete之后的事件将会继续发送, ⽽Observer收到onComplete事件之后将
//        不再继续接收事件.
//        3．当Observable发送了⼀个onError后, Observable中onError之后的事件将继续发送, ⽽Observer收到onError事件之后将不再继续接收事
//        件.
//        4．Observable可以不发送onComplete或onError.
//        5．不能 onComplete 后调⽤ onError，onComplete和onError必须唯⼀并且互斥
//注意导包io.reactivex  这个包下
         //被观察者   注意是creater, 不是new 出来的
        Observable<String> observable=Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                //ObservableEmitter 发射器
       //如果观察者接受oncomplite或者onerror ,,这两个方法之后的事件，观察者是接受不到的
                e.onNext("1");  //发射器发射后   在观察者的onNext 方法中 可以收到发射的内容
             //   disposable.dispose();  //通过这个取消 被观察者和观察者的关联 。取消后，的事件，观察者也是接受不到的
                e.onNext("2");
              //  e.onComplete(); //完成  完成之后发送的信息，，观察者是收不到的  因为发送完成了，所以在观察者中接受不到
               // System.out.println("e = " + e); //在完成之后，这个输出还是可以执行的
              //  e.onError(new Resources.NotFoundException());//发送一个错误，结果和onComPlete 一样，观察者走onerror方法
              //   System.out.println("e = " + e); //在完成之后，这个输出还是可以执行的
                System.out.println("被观察者 = " + Thread.currentThread().getName());
            }
        });
            //观察者
        Observer<String> observer=new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable=d;
                System.out.println("\"onSubscribe\" = " + "onSubscribe");
            }
//接受 被观察者 发来的信息
            @Override
            public void onNext(@NonNull String o) {
                System.out.println("o = " + o);
                System.out.println("观察者 = " + Thread.currentThread().getName());
            }
            @Override
            public void onError(@NonNull Throwable e) {
                System.out.println("\"onError\" = " + "onError");
            }
            @Override
            public void onComplete() {
                System.out.println("\"onComplete\" = " + "onComplete");
            }
        };
        //必须有订阅哦  让观察者和被观察者产生关联
        observable.subscribe(observer);
    }


    //观察者和被观察者放在一起了；
    public void text02(){
        //        subscribeOn 指定被观察者所在的线程
      //        observeOn 指定观察者所在的线程
      //        Schedulers 调度器
        //Integer 被观察者指定信息类型，观察者接受信息类型
     Observable.create(new ObservableOnSubscribe<Integer>() {
          @Override
          public void subscribe(@NonNull ObservableEmitter e) throws Exception {
              e.onNext(1);
              e.onNext(2);
              e.onNext(3);
              System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
          }
      }).subscribeOn(Schedulers.io())   //Schedulers 调度器:调度线程的
                    // Schedulers.computation()      比较费cpu的用这个
                   // Schedulers.newThread()      普通线程
                                      // subscribeOn 指定被观察者所在的线程   常为子线程
        .observeOn(AndroidSchedulers.mainThread())    //observeOn 指定观察者所在的线程  常为主线程
         .subscribe(new Observer<Integer>() {  //产生关联
          @Override
          public void onSubscribe(@NonNull Disposable d) {
          }
          @Override
          public void onNext(@NonNull Integer integer) {
              System.out.println("integer = " + integer);
              System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
          }
          @Override
          public void onError(@NonNull Throwable e) {
          }
          @Override
          public void onComplete() {
          }
      });
    }
    //    map 变换操作符    //map 的作用是将被观察者 发的信息，进行翻译加工，形成新的信息  一对一
    public void map(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("1");
            }
//            String 第一个参数  onNext 发送数据的一个类型 被观察者发的数据类型   例子：图片的url
//            Integer  第二个参数  经过map变换 所产生的数据类型   观察者接受的数据类型  例子 bitmap

        }).map(new Function<String, Integer>() {
            @Override       //返回值类型为观察者接受的数据类型   参数为：被观察者发的数据类型
            public Integer apply(@NonNull String s) throws Exception {
                System.out.println("map apply = " + s);
                return Integer.valueOf(s);
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }
            @Override
            public void onNext(@NonNull Integer integer) {
                System.out.println("subscribe onNext = " + integer);
            }
            @Override
            public void onError(@NonNull Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }
    //    flatMap 无序的，产生数据源  或产生一个集合 变换操作符          一对多
    public  void flatMap(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                    e.onNext("wang");
                    e.onNext("wang1");
                    e.onNext("wang2");
            }
        })
         .flatMap(new Function<String, ObservableSource<ArrayList>>() {  //ObservableSource 数据源
             @Override
             public ObservableSource<ArrayList> apply(@NonNull String s) throws Exception {
                 ArrayList list = new ArrayList<>();
                 for (int i=0;i<10;i++){
                     list.add(i+s);
                 }
                 return Observable.fromArray(list).delay(2, TimeUnit.SECONDS);//delay(2, TimeUnit.SECONDS)  延时进入观察者（为了方便解释无序的）
             }
         })
       .subscribe(new Observer<ArrayList>() {
           @Override
           public void onSubscribe(@NonNull Disposable d) {
           }
           @Override
           public void onNext(@NonNull ArrayList arrayList) {
               System.out.println(arrayList.size()+"daxiao");
               for(int i=0;i<arrayList.size();i++){
                   System.out.println(arrayList.get(i));
               }
           }
           @Override
           public void onError(@NonNull Throwable e) {
           }
           @Override
           public void onComplete() {
           }
       });
    }
    //    concatMap 有序的  与flatMap相区别  这个是有序的
    public void concatmap(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("1");
                e.onNext("2");
                e.onNext("3");
                e.onNext("4");
                e.onNext("5");
            }
        }).concatMap(new Function<String, ObservableSource<ArrayList>>() {
            @Override
            public ObservableSource<ArrayList> apply(@NonNull String s) throws Exception {
                ArrayList list = new ArrayList();
                for (int i = 0; i < 5; i++) {
                    list.add(s + "  " + i);
                }
                return Observable.fromArray(list).delay(1, TimeUnit.SECONDS);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ArrayList>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }
            @Override
            public void onNext(@NonNull ArrayList o) {
                for (int i = 0; i < o.size(); i++) {
                    System.out.println("o = " + o.get(i));
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }
    public void map1(){
//        Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
//
//            }
//        }).map(new Function<String, ObservableSource<ArrayList>>() {
//            @Override
//            public ObservableSource<ArrayList> apply(@NonNull String s) throws Exception {
//                return null;
//            }
//        }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<ObservableSource<ArrayList>>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                    }
//                    @Override
//                    public void onNext(@NonNull ObservableSource<ArrayList> arrayListObservableSource) {
//                        arrayListObservableSource.subscribe(new Observer<ArrayList>() {
//                            @Override
//                            public void onSubscribe(@NonNull Disposable d) {
//                            }
//                            @Override
//                            public void onNext(@NonNull ArrayList arrayList) {
//                            }
//                            @Override
//                            public void onError(@NonNull Throwable e) {
//                            }
//                            @Override
//                            public void onComplete() {
//                            }
//                        });
//                    }
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                    }
//                    @Override
//                    public void onComplete() {
//                    }
//                });
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("1");
                e.onNext("2");
                e.onNext("3");
                e.onNext("4");
                e.onNext("5");
//                e.onComplete();
            }
        }).map(new Function<String, ArrayList>() {
            @Override
            public ArrayList apply(@NonNull String s) throws Exception {
                ArrayList list = new ArrayList();
                for (int i = 0; i < 5; i++) {
                    list.add(s + "  " + i);
                }
                return list;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }
                    @Override
                    public void onNext(@NonNull ArrayList o) {
                        for (int i = 0; i < o.size(); i++) {
                            System.out.println("o = " + o.get(i));
                        }
                    }
                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }
    //压缩合并
    public void zip(){
        //两者的合成的最后长度以 两者中，最短的为标准
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("1");
                e.onNext("2");
                e.onNext("3");
                e.onNext("4");
            }
        })  ;
        Observable observable1 = Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(@NonNull ObservableEmitter e) throws Exception {
                e.onNext("A");
                e.onNext("B");
                e.onNext("C");
                e.onNext("D");
                e.onNext("E");
            }
        }) ;
        //开始合并  String,String,String  ，第一个数据的类型，第二个数据的 类型，最终结果的类型
        Observable.zip(observable, observable1, new BiFunction<String,String,String>() {
            @Override
            public String apply(@NonNull String o, @NonNull String o2) throws Exception {
                return o + " ----- " + o2;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }
                    @Override
                    public void onNext(@NonNull Object o) {
                        System.out.println("o = " + o);
                    }
                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }
    //过滤
    public void fliter(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for(int i=0;i<100;i++){
                    e.onNext(i);
                }
            }
        }).filter(new Predicate<Integer>() {
            @Override
            public boolean test(@NonNull Integer integer) throws Exception {
//过滤
                //true  表示数据是我们需要的
//                false 不需要的
                return integer % 10 == 0;
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }
            @Override
            public void onNext(@NonNull Integer integer) {
                System.out.println("integer = " + integer);
            }
            @Override
            public void onError(@NonNull Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }
  //Consumer   //这个accept  相当于onNext
    public void consumer(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                for(int i=0;i<10;i++){
                    e.onNext(i+" ");
                }
            }
        }).subscribe(new Consumer<String>() {
            @Override   //这个accept  相当于onNext
            public void accept(@NonNull String s) throws Exception {
                System.out.println("s = " + s);
            }
        });
    }
    public void consumer1(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                for(int i=0;i<10;i++){
                    e.onNext(i+" ");
                }
                e.onError(new NullPointerException());
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                System.out.println("accept onnext = " + s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                System.out.println("accept onerror = ");
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                System.out.println("accept oncomplete = " );
            }
        }, new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                System.out.println("accept Disposable = " );
            }
        });
    }
//测试 内存   在android monitor 的 monitors 里看 程序所占的内存
    public void memory(){
        //被观察者发的 比观察者收到的信息的速度快
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for(int i=0;i<10000000;i++){
                    e.onNext(i);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                System.out.println("integer = " + integer);
            }
        });
    }
    //    Flowable 支持背压   被压：解决被观察者比观察者快，通过降低被观察者的速度 来解决这个问题
    public void flowable(){
//支持背压 的被观察者
        //Flowable默认队列大小为128
        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                for(int i=0;i<140;i++){
                    e.onNext(i + "");
                }
            }
        }, BackpressureStrategy.LATEST)   //这里有个默认 缓存值，大小为128  如果我们的这个策略大小大于128 会报  BUFFER DROP  ERROR  MISSING
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println("s = " + s);
                    }
                });
    }
    public void flowable1(){
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
            }
        },BackpressureStrategy.ERROR).subscribe(new FlowableSubscriber<Integer>() {
            @Override
            public void onSubscribe(@NonNull Subscription s) {
                //观察者 可以 接受 被观察者 发送事件 的次数   Integer.MAX_VALUE
                s.request(Integer.MAX_VALUE);
            }
            @Override
            public void onNext(Integer integer) {
                System.out.println("integer = " + integer);
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        });
    }
    public void just(){
//        Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
//                e.onNext("1");
//            }
//        }).subscribe(new Consumer<String>() {
//            @Override
//            public void accept(@NonNull String s) throws Exception {
//            }
//        });
        Observable.just("1","2","3").subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                System.out.println("s = " + s);
            }
        });
    }
    public void total(){
        Observable<String> observable1 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("1");
            }
        });
        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("A");
            }
        });
        Observable.zip(observable1, observable2, new BiFunction<String, String, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull String s2) throws Exception {
                return null;
            }
        }).map(new Function<String, String>() {
            @Override
            public String apply(@NonNull String s) throws Exception {
                return null;
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                return null;
            }
        }).filter(new Predicate<String>() {
            @Override
            public boolean test(@NonNull String s) throws Exception {
                return false;
            }
        }).concatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                    }
                });
    }
}