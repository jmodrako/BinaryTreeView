BinaryTreeView
==============

Simple three classes which let you to browse your data as binary tree. So, if you have data model as binary tree, 
these classes will be usefull for you.

*Here is sample [application APK](https://play.google.com/store/apps/details?id=pl.modrakowski.android).*

Usage
-------------------------

To build your own tree, just copy following classes to your project:
  1. ```UserViewWrapper```
  2. ```UserBackgroundView``` 
  3. ```UserForegroundView```

It also require some extra attributes in ```attrs.xml``` file:
```xml    
<declare-styleable name="UserViewWrapperAttrs">
  <!-- user_type describe kind of view. -->
  <attr name="user_type">
    <enum name="parent" value="1"/>
    <enum name="leftChild" value="2"/>
    <enum name="rightChild" value="3"/>
  </attr>
  <!-- open_direction describe how view can be opened through finger slide. -->
  <attr name="open_direction">
    <enum name="left" value="1"/>
    <enum name="right" value="2"/>
    <enum name="none" value="3"/>
  </attr>
  <!-- outside_move describe whether or not view can move outside of the screen. -->
  <attr name="outside_move" format="boolean"/>
  
  <!-- move_direction describe how view can be moved up/down. -->
  <attr name="move_direction">
    <enum name="up" value="1"/>
    <enum name="down" value="2"/>
    <enum name="both" value="3"/>
    <enum name="none" value="4"/>
  </attr>
</declare-styleable>
```

Code below describes simple usage wrapper's classes:
```xml
xmlns:binarytree="http://schemas.android.com/apk/res/your.packagename.here"

<!-- Parent view. -->
<UserViewWrapper>
  ...
  binarytree:user_type="parent"
  binarytree:open_direction="right"
  binarytree:outside_move="false"
  binarytree:move_direction="down">
  ...

  <!-- Parent background view. -->
  <UserBackgroundView>
    ...
    here you can put your own background layout
    ...
  </UserBackgroundView>
  
  <!-- Parent foreground view. -->
  <UserForegroundView>
    ...
    here you can put your own foreground layout
    ...
  </UserForegroundView>

</UserViewWrapper>
```
        
![binarytree first](https://raw.github.com/jmodrako/BinaryTreeView/master/img/first.png)
![binarytree second](https://raw.github.com/jmodrako/BinaryTreeView/master/img/second.png)
![binarytree third](https://raw.github.com/jmodrako/BinaryTreeView/master/img/third.png)
![binarytree third](https://raw.github.com/jmodrako/BinaryTreeView/master/img/four.png)
![binarytree third](https://raw.github.com/jmodrako/BinaryTreeView/master/img/five.png)
![binarytree third](https://raw.github.com/jmodrako/BinaryTreeView/master/img/six.png)

Known issues:
1. setVisibility(visible/invisible) - does't work in android 2.1.
