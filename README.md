# uexWebBrowser

## 介绍

浏览器插件


## changelog

### 4.3.2

增加接口用于全局初始化内核（仅Android。本接口整个App生命周期内必需，也只需要初始化一次。需要在隐私权限声明同意后初始化。）：initGlobalWebCore([function(cbType, data)])

function回调参数如下：
cbType: String, 当等于onCoreInitFinished时代表X5内核初始化完成。
data: 其他情况下的数据，可以忽略。
