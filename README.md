[![Download](https://api.bintray.com/packages/jiewang19951030/Maven/Muti-Barrage/images/download.svg)](https://bintray.com/jiewang19951030/Maven/Muti-Barrage/_latestVersion)[![](https://img.shields.io/badge/license-Apache2.0-green.svg)](https://opensource.org/licenses/apache2.0.php) 

# Muti-Barrage

🌠 Muti-Barrage 一个轻量级、自定义多视图、碰撞检测和触摸事件处理的弹幕库

- [x] 自定义多视图
- [x] 设置发送间隔
- [x] 碰撞检测（多视图情况下还存在问题）
- [x] 触摸事件处理
- [x] 支持全屏和上中下显示

## 一、效果

写这个库的初衷就是觉得QQ空间中有弹幕的图片预览看上去很好玩，所以就打算实现一个，很早之前我就写了一个图片预览库，但我并不打算直接在原有的库中直接写弹幕代码，而是直接新建一个库，方便学习。不过现在我的图片预览库已经通过依赖`Muti-Barrage`集成弹幕功能了：[PhotoPagerView](<https://github.com/mCyp/PhotoPagerView>)

![QQ主题](<https://github.com/mCyp/PhotoPagerView/blob/master/pic/%E5%9B%BE%E7%89%87%E5%BA%93QQ%E4%B8%BB%E9%A2%98.gif>)

#### 1.  单视图

#### 2. 多视图

## 二、使用方法

#### 1. 添加依赖

  先在build.gradle(Project:xxx)的repositories中添加：

```
  allprojects {
    repositories {
        google()// 不翻墙可以注释掉
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```

  然后在 build.gradle(Module:app) 的 dependencies 添加:

```
 dependencies {
    ...
   
    /*需要添加的依赖 这里可以查看一下上面的最新版本*/
    implementation 'com.jieWang:Muti-Barrage:xxx'
 }
```

#### 2. 使用

使用方法有点跟`RecyclerView`相似，可自由定制也造成了使用成本的上升，没办法～

**第一步：添加进布局文件**

```xml
<com.orient.tea.barragephoto.ui.BarrageView
        android:id="@+id/barrage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

**第二步：构建自己的弹幕数据类型实现`DataSource`接口**

```java
public class BarrageData implements DataSource {
		...
		@Override
    public int getType() {
        return type;
    }

    // 如果需要，通常是不需要的，后续会删除
    @Override
    public long getShowTime() {
        return 0;
    }
}
```

**第三步：获取BarrageView并初始化参数**

```java
private BarrageView barrageView;

...
  
barrageView = findViewById(xxx);
BarrageView.Options options = new BarrageView.Options()
                .setGravity(BarrageView.GRAVITY_TOP)                // 设置弹幕的位置
                .setInterval(50)                                     // 设置弹幕的发送间隔
                .setSpeed(200,29)                   // 设置速度和波动值
                .setModel(BarrageView.MODEL_COLLISION_DETECTION)     // 设置弹幕模式 随机生成or碰撞检测
                .setClick(false);                                    // 设置弹幕是否可以点击
barrageView.setOptions(options);
```

**第四步：创建`ViewHolder`，实现`BarrageViewHolder`接口（后面多处需要加范型）**

```java
// 在多视图弹幕中自己需要构建多个类型ViewHolder
class ViewHolder extends BarrageAdapter.BarrageViewHolder<BarrageData> {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(BarrageData data) {
            ...// 自己想在ViewHolder中对Ui的处理
        }
}
```

**第五步：设置适配器**

创建适配器，需要加上范型（第二部实现的数据类型）

```
private BarrageAdapter<BarrageData> mAdapter;
```

单视图

```java
barrageView.setAdapter(mAdapter = new BarrageAdapter<BarrageData>(null, this) {
            @Override
            public BarrageViewHolder<BarrageData> onCreateViewHolder(View root, int type) {
                return new SingleBarrageActivity.ViewHolder(root);// 返回自己创建的ViewHolder
            }

            @Override
            public int getItemLayout(BarrageData barrageData) {
                return R.layout.barrage_item_normal;// 返回自己设置的布局文件
            }
});
```

多视图要麻烦一点（可以查看示例代码）

```java
// 设置适配器 第一个参数是点击事件的监听器
barrageView.setAdapter(mAdapter = new BarrageAdapter<BarrageData>(null, this) {
            @Override
            public BarrageViewHolder<BarrageData> onCreateViewHolder(View root, int type) {
                switch (type) {// 这里的type指的我们设置的子布局文件，然后设置ViewHolder
                    ... // 不同的布局文件构建不同的ViewHolder
                }
            }

            @Override
            public int getItemLayout(BarrageData barrageData) {
                switch (barrageData.getType()) {// 根据弹幕数据中的type设置子布局文件
                    ... // 不同的弹幕类型返回不同的布局文件
                }
            }
});
```

如果需要设置弹幕触摸事件，第三步中`BarrageView.Options`必须得设置可点击

```java
// 设置监听器
mAdapter.setAdapterListener(new AdapterListener<BarrageData>() {
            @Override
            public void onItemClick(BarrageAdapter.BarrageViewHolder<BarrageData> holder, BarrageData item) {
              ...
            }
});
```

## 三、TODO

- [ ] 竖直方向的弹幕

## 四、感谢

[XDanmuku](<https://github.com/hust201010701/XDanmuku>)

弹幕获取最佳弹道及弹幕行数的设置参考自该库，感谢作者

## License

```
  Copyright 2019 JieWang.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```



