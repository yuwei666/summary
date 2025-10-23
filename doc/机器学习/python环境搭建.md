Kaggle 是一个面向数据科学家、机器学习从业者和分析师的在线平台，提供了大量的数据集、竞赛、代码共享以及学习资源。

**Notebooks**：Kaggle 提供了基于云的环境，允许用户在平台上直接运行代码，而不需要本地设置环境。这使得用户可以快速测试和分享模型和分析过程。

如果想执行开源代码，如在本地跑模型，就需要本地有Notebooks环境，使用Pycharm直接连接远程jupyter服务器即可。



1. 拉取镜像

```
docker pull kaggle/python:latest
```

2. 启动容器，包括暴露8888端口，挂载目录，设置工作空间-w /workspace

```bash
sudo docker run -d -p 8888:8888 --name kaggle_python --restart always --gpus '"device=1"' \
-v /home/yuwei/dataSet/kaggle_python:/workspace/dataSet -w /workspace \
kaggle/python jupyter notebook --ip=0.0.0.0 --allow-root --no-browser
```

3. 容器启动成果后，查看日志中的密钥token

```bash
docker logs kaggle_python
```

```tex
|CRITICAL|
    
    To access the notebook, open this file in a browser:
        file:///root/.local/share/jupyter/runtime/nbserver-1-open.html
    Or copy and paste one of these URLs:
        http://3023136e7825:8888/?token=db0976b19d3cb651da53ff6f2037f6d504d1fead4d515243
     or http://127.0.0.1:8888/?token=db0976b19d3cb651da53ff6f2037f6d504d1fead4d515243
```

4. 在Pycharm中使用远程服务器，带上token

​	设置 -> 搜索 "Jupyter服务器" -> 配置的服务器 

​	输入：http://172.16.1.106:8888/ 连接后弹窗输入token即可



5. 安装（可选）

   安装docker-nvidia2

```
```

