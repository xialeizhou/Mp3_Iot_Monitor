# Mp3_Iot_Monitor
Mp3 Iot Monitor和MP3无关, 是一个实时接收环境参数(温度, 加速度), 通过安卓USB Accessory读取, 处理后发送到后端并提供前端UI展示页面的
项目, 是我和朋友合作完成的墨尔本大学研究生capstone(毕业设计) project.
共分为两部分: Mp3_Iot_Monitor和Mp3_Iot_Service.
**Mp3_Iot_Monitor**是一个基于android开发的app, [Mp3_Iot_Service](https://github.com/xialeizhou/Mp3_IOT_Service)为app提供后台查询,写入的RESTfull服务.

## 用到的库
- volly: 异步发送HTTP POST请求
- hellocharts: 复杂图表显示

## App数据流
传感器信号-> USB Accessory -> monitor -> process -> volly send http post -> RESTful API -> jdbc -> mysql

## UI端展示
- ByteRead monitor(监控器)
- RealyTime data display(写入数据实时柱状图显示)
- Anomalous point display (异常点显示, 用到部分SVM算法)

