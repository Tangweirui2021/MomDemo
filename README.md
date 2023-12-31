## 软件架构与中间件实验一

### 全广播式
#### 启动方式
- Server直接启动即可
- Client需要使用环境变量指定行为
  - 环境变量 PUB
    - 指定发布的消息
    - 示例: PUB=A 12:0,B 23:2,C 34:1 代表发布A 12,B 23,C 34三条信息，A立即发送，B在A后延迟2秒发送，C在B后延迟1秒发送
  - 环境变量 REPEAT
      - REPEAT为true时代表重复发送PUB的消息
  - 环境变量 SUB
    - 指定订阅的消息
    - 示例: SUB=A,B,C 代表订阅A,B,C三类消息

### 选择广播式-点对点
#### 启动方式
- Server直接启动即可
- Client需要使用环境变量指定行为
  - 环境变量 PUB
    - 格式与全广播相同
    - 在信息中使用%id，在运行时会被替换成发送的信息的id
  - 环境变量 REPEAT
    - 与全广播相同
  - 环境变量 SUB
    - 创建订阅的消息类型
    - 示例: SUB=A,B,C 代表创建A,B,C三类消息的队列
  - 环境变量 GET
    - 循环获取订阅的消息类型
    - 示例: GET=A,B,C 代表循环获取A,B,C三类消息
  - 环境变量 WAIT
    - 指定循环获取消息的间隔时间
    - 示例: WAIT=1000 代表每次循环获取消息等待1000毫秒

### 选择广播式-发布订阅
#### 启动方式
- Server直接启动即可
- Client需要使用环境变量指定行为
  - 环境变量 PUB
    - 示例: PUB=A 12:1:0,B 23:3:2,C 34:2:1 代表发布A 12,B 23,C 34三条信息，<br>A立即发送，B在A后延迟2秒发送，C在B后延迟1秒发送 <br>A的有效期为1秒，B的有效期为3秒，C的有效期为2秒
    - 在信息中使用%id，在运行时会被替换成发送的信息的id
  - 环境变量 REPEAT
    - 与全广播相同
  - 环境变量 SUB
    - 创建订阅的消息类型
    - 示例: SUB=A,B,C 代表创建A,B,C三类消息的队列