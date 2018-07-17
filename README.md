# hxgbus

#### 引入方式

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
    
    
dependencies {
    implementation 'com.github.huangxiaoguo1:hxgbus:1.0.0'
}
```

#### 注册

```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    
        HxgBus.getDefault().register(this);
    
    }


```

#### 清除注册

```
    @Override
    protected void onDestroy() {
    
        HxgBus.getDefault().unregister(this);
   
    }
    
```
#### 接收数据

###### 方式一（什么都没带）

```
    /**
     * 什么都没带
     * 此时，数据接收依参数类型决定
     * @param name
     */
    @Subscriber()
    public void ShowNameNo(String name) {
        Log.e("huangxiaoguo", name + "---什么都没带");
    }

```

###### 方式二（带有tag标记）

```
    /**
     * 带有tag标记 
     * 此时 数据接收依tag决定
     * @param name
     */
    @Subscriber(tag = Contant.tag)
    public void ShowNameTag1(String name) {
        Log.e("huangxiaoguo", name + "---只带有tag");
    }
    
```

###### 方式三（四种不同类型的线程接收，并带有优先级）

```
    /**
     * 主线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.MAIN, priority = 100)
    public void ShowName(String name) {
        Log.e("huangxiaoguo", name + "===100");
    }
    
    /**
     * 异步线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.ASYNC, priority = 500)
    public void ShowName2(String name) {
        Log.e("huangxiaoguo", name + "===500");
    }
    
    /**
     * 子线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.BACKGROUND, priority = 600)
    public void ShowName3(String name) {
        Log.e("huangxiaoguo", name + "===600");
    }
    
    /**
     * 相同线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.POSTING, priority = 700)
    public void ShowName4(String name) {
        Log.e("huangxiaoguo", name + "===700");
    }
```
###### 方式四（指定线程，指定tag，指定优先级）

```
    /**
     * 主线程，带有tag标记
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.MAIN, tag = Contant.tag, priority = 100)
    public void ShowNameTag(String name) {
        Log.e("huangxiaoguo", name + "---带有tag===100");
    }
    
```

#### post数据


###### 不带tag
```
    HxgBus.getDefault().post( "111");
    
```
###### 带有tag
```

    HxgBus.getDefault().post( Contant.tag, "111");

```