| 场景 | 影响 | 降级 | 原因 | 
| :--------: |:-------: | :-----: | :-----: |
| 某台Config Service下线 | 无影响 | / |Config Service无状态，客户端重连其它Config Service | 
| 一个机房的Config Service下线 | 无影响 | 客户端就近连接Config Service | Config Service无状态且全球多机房部署 | 
| 所有Config Service下线 | 客户端无法读取最新配置，Portal无影响 | 客户端重启时，可以读取本地缓存配置文件。如果是新扩容的机器，可以从其它机器上获取已缓存的配置文件 | / |
| 某台Admin Service下线 | 无影响 | / | Admin Service无状态，Portal重连其它Admin Service | 
| 所有Admin Service下线 | 客户端无影响，Portal无法更新配置 |无法降级 | /|	
| 某台Portal下线 | 无影响 | / | Portal域名通过SLB绑定多台服务器，重试后指向可用的服务器 | 
| 全部Portal下线 | 客户端无影响，Portal无法更新配置 | / | /|
| 某个数据中心下线 | 无影响 | / |  多数据中心部署，数据完全同步，Meta Server/Portal域名通过SLB自动切换到其它存活的数据中心 | 
| 数据库主节点挂掉 | 客户端无影响；Portal无法更新配置，但是会在10S内自动恢复 | / | MGR集群通过Paxos协议选出另一个主节点（3S左右），Admin Service会在秒级时间内（5S左右）检测到主节点变化，并切换 |
| 数据库主节点MGR全部挂掉 | 客户端无影响；Portal无法更新配置 | / | 各个机房的Config Service连接各自的从库读取配置 |
| 某个机房的从节点挂掉 | 无影响 | 从节点有备用节点，会秒级切换过去 | Config Service会在秒级检测到从不不可用，并切换到备用从库 |
| 某个机房的从节点全挂掉 | 连接到该机房的客户端无法收到配置推送 | Config Service开启配置缓存后，对配置的读取不受数据库宕机影响 | / |
| 数据库全部挂掉 | 客户端无影响；Portal无法更新配置 | Config Service开启配置缓存后，对配置的读取不受数据库宕机影响 | / |
| meta域名被封 |客户端无影响，Portal无影响| / | 客户端通过meta获取Config Service列表，并在本地缓存；Portal 客户端通过meta获取Admin Service列表，并在本件缓存|
