1. 下载Clash Meta（mihomo 内核）

   wget -O clash.meta.gz https://github.com/MetaCubeX/mihomo/releases/latest/download/mihomo-linux-amd64-v1.gz

   如果无法下载，就去github上下载完，上传到服务器，改名clash.meta.gz。

2.  解压并赋权

   ```bash
   gunzip clash.meta.gz
   chmod +x clash.meta
   ```

3. 运行

   ```bash
   ./clash.meta -f config.yaml
   # 如果配置文件在 ~\.conf\clash，直接执行即可
   ./clash.meta
   
   # 还可以将此文件移动到/usr/sbin下，便于直接执行
   ```

​	config.yaml的获取

```
cd ~/.config/clash/ 

wget -O config.yaml "订阅地址" 
```

​	这个订阅地址购买代理的网站上提供的，都是加密的，无法直接使用。找一个[订阅转换](https://acl4ssr-sub.github.io/)网站，转换成Clash格式的url，然后粘贴，就可以正常下载了。

```bash
# 设置变量，这样 wget / curl / git / pip 等命令都会走代理。
export http_proxy=http://127.0.0.1:7890
export https_proxy=http://127.0.0.1:7890

测试
curl https://www.google.com
```



4. 开机自启

```bash
mkdir -p ~/.config/systemd/user
nano ~/.config/systemd/user/clash.service
```

```ini
[Unit]
Description=Clash Meta Proxy Service
After=network.target

[Service]
Type=simple
ExecStart=/home/yuwei/clash.meta -d /home/yuwei/.config/clash
Restart=on-failure
RestartSec=5
StandardOutput=append:/home/yuwei/clash.log
StandardError=append:/home/yuwei/clash.log

[Install]
WantedBy=default.target
```

**说明：**

- `ExecStart` → Clash Meta 可执行文件路径
- `-d /home/yuwei/.config/clash` → 配置目录
- `Restart=on-failure` → 进程意外退出自动重启
- 日志输出到 `~/clash.log`

```bash
# 重新加载 systemd 用户服务
systemctl --user daemon-reload

# 设置开机自动启动
systemctl --user enable clash.service

# 立即启动服务（测试）
systemctl --user start clash.service
# 查看状态
systemctl --user status clash.service
```

Clash Meta 会在 yuwei 用户登录后自动启动

运行在用户空间，不需要 sudo

日志在 `~/clash.log`，方便排查

系统启动后，yuwei 用户打开终端或 GUI 登录就会自动运行 Clash



5. 通过脚本控制 

   创建文件：clashctl.sh

   ```bash
   #!/bin/bash
   
   # ================= 配置区域 =================
   CLASH_META="$HOME/clash.meta"          # Clash Meta 可执行文件
   CLASH_CONFIG_DIR="$HOME/.config/clash" # Clash 配置目录
   PORT=7890                               # Mixed-Port
   LOG_FILE="$HOME/clash.log"
   
   # ================= 函数 =================
   start() {
       if lsof -i :"$PORT" >/dev/null 2>&1; then
           echo "[INFO] Clash Meta 已经在运行，端口 $PORT 被占用"
       else
           echo "[INFO] 启动 Clash Meta..."
           nohup "$CLASH_META" -d "$CLASH_CONFIG_DIR" >"$LOG_FILE" 2>&1 &
           sleep 2
           if lsof -i :"$PORT" >/dev/null 2>&1; then
               echo "[INFO] Clash Meta 启动成功"
           else
               echo "[ERROR] Clash Meta 启动失败，请检查配置"
           fi
       fi
   }
   
   stop() {
       PID=$(lsof -t -i :"$PORT")
       if [ -z "$PID" ]; then
           echo "[INFO] Clash Meta 未运行"
       else
           echo "[INFO] 停止 Clash Meta (PID: $PID)..."
           kill -9 $PID
           echo "[INFO] Clash Meta 已停止"
       fi
   }
   
   status() {
       if lsof -i :"$PORT" >/dev/null 2>&1; then
           echo "[INFO] Clash Meta 正在运行"
       else
           echo "[INFO] Clash Meta 未运行"
       fi
   }
   
   restart() {
       stop
       sleep 1
       start
   }
   
   # ================= 主程序 =================
   case "$1" in
       start) start ;;
       stop) stop ;;
       restart) restart ;;
       status) status ;;
       *) echo "用法: $0 {start|stop|restart|status}" ;;
   esac
   
   ```

   赋予权限

   ```bash
   chmod +x ~/clashctl.sh
   ```

   使用：

   ```bash
   # 启动
   ./clashctl.sh start
   
   # 停止
   ./clashctl.sh stop
   
   # 查看状态
   ./clashctl.sh status
   
   # 重启
   ./clashctl.sh restart
   ```

   只针对当前用户 yuwei，无需 sudo

   自动检查端口占用，避免重复启动

   日志写入 `~/clash.log`，方便调试