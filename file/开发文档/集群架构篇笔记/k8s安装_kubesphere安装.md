[TOC]

* * *

# 灰灰商城-分布式高级篇-14

## k8s（kubernetes）

### 环境准备

#### 1、创建3个虚拟机（使用VMware）

vmware创建3个centos7虚拟机
配置静态ip

k8s-node1:
centos7 ip:192.168.80.136
配置：cd /etc/syscofig/network-script
vi ifcfg-ens33
修改配置：
BOOTPROTO=static
ONBOOT=yes
IPADDR=192.168.80.136
GATEWAY=192.168.80.2
DNS1=192.168.80.2

k8s-node2:
centos7 ip:192.168.80.137
配置：cd /etc/syscofig/network-script
vi ifcfg-ens33

k8s-node3:
centos7 ip:192.168.80.137
配置：cd /etc/syscofig/network-script
vi ifcfg-ens33

systemctl restart network

#### 尝试使用VirtualBox

创建一个虚拟机代码:

```c
Vagrant.configure("2") do |config|
   
	config.vm.define "k8s-node3" do |node|
		# 设置虚拟机的Box
		node.vm.box = "centos/7"

		#下载地址
		config.vm.box_url = "https://mirrors.ustc.edu.cn/centos-cloud/centos/7/vagrant/x86_64/images/CentOS-7.box"
		
		# 设置虚拟机的主机名
		node.vm.hostname="k8s-node3"

		# 设置虚拟机的IP
		node.vm.network "private_network", ip: "192.168.56.102", netmask: "255.255.255.0"

		# 设置主机与虚拟机的共享目录
		# node.vm.synced_folder "~/Documents/vagrant/share", "/home/vagrant/share"

		# VirtaulBox相关配置
		node.vm.provider "virtualbox" do |v|
			# 设置虚拟机的名称
			v.name = "k8s-node3"
			# 设置虚拟机的内存大小
			v.memory = 4096
			# 设置虚拟机的CPU个数
			v.cpus = 4
		end
   end
end
```

连入虚拟机，开启密码访问（密码vagrant）
vi /etc/ssh/sshd_config

把PasswordAuthentication改为 yes
重启：service sshd restart


#### 2、设置Linux环境

关闭防火墙：
systemctl stop firewalld
systemctl disable firewalld

* * *

关闭 selinux：（linux默认安全策略）
sed -i 's/enforcing/disabled/' /etc/selinux/config
setenforce 0

* * *

关闭swap：（内存交换）
swapoff -a （临时关，重启还有）
sed -ri 's/.*swap.*/#&/' /etc/fstab 永久
free -g 验证，swap必须为0

* * *

添加主机名与ip对应关系
vi /etc/hosts
10.0.2.5 k8s-node1
10.0.2.4 k8s-node2
10.0.2.15 k8s-node3

指定新的主机名：

hostnamectl set-hostname newhostname

newhostname为新的hostname

* * *

将桥接的ipv4流量传递到iptables的链

cat > /etc/sysctl.d/k8s.conf << EOF
net.bridge.bridge-nf-call-ip6tables=1
net.bridge.bridge-nf-call-iptables=1
EOF

sysctl --system

* * *

疑难问题：
遇见提示是只读的文件系统，运行如下命令
mount -o remount rw /

* * *

date 查看时间（可选）
yum install -y ntpdate
ntpdate time.windows.com 同步最新时间

### 所有节点安装Docker,kubeadm,kubelet,kubectl

Kubernetes默认CRI（容器运行时）为docker,因此先安装docker

#### 1. 安装docker

安装文档
https://docs.docker.com/engine/install/centos/


1. 卸载系统之前的docker


sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

2. 安装Docker-CE

安装必须的依赖

sudo yum install -y yum-utils \
device-mapper-persistent-data \
lvm2

* * *

设置docker repo 的yum 位置

sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
    
- 如果慢：
#阿里云源地址# http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

* * *

安装docker,以及docker-cli

sudo yum install -y docker-ce docker-ce-cli containerd.io

* * *

3. 配置docker加速

sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "registry-mirrors": ["https://kpw8rst3.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker

```
cat > /etc/docker/daemon.json <<EOF
{
	"exec-opts": ["native.cgroupdriver=systemd"],
	"log-driver": "json-file",
	"log-opts": {
		"max-size": "100m"
	},
	"storage-driver": "overlay2",
	"registry-mirrors": ["https://kpw8rst3.mirror.aliyuncs.com"]
}
EOF
```

4. 启动docker&设置docker开机自启

systemctl enable docker


#### 2.添加阿里云yum源（安装ks8s源）

```
cat > /etc/yum.repos.d/kubernetes.repo << EOF
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
```
检查是否有：
yum list|grep kube

安装：
yum install -y kubelet-1.17.3 kubeadm-1.17.3 kubectl-1.17.3

自启：
systemctl enable kubelet
systemctl start kubelet



### 部署k8s-master

#### 1、master节点初始化

虚拟机至少2核2G

kubeadm init \
--apiserver-advertise-address=10.0.2.15 \
--image-repository registry.cn-hangzhou.aliyuncs.com/google_containers \
--kubernetes-version v1.17.3 \
--service-cidr=10.96.0.0/16 \
--pod-network-cidr=10.244.0.0/16

```
kubeadm init \
--apiserver-advertise-address=0.0.0.0 \
--apiserver-bind-port 6443 \
--cert-dir /etc/kubernetes/pki \
--control-plane-endpoint kuber4s.api \
--image-repository registry.cn-hangzhou.aliyuncs.com/google_containers \
--kubernetes-version v1.17.3 \
 --pod-network-cidr 10.10.0.0/16 \
 --service-cidr 10.20.0.0/16 \
 --service-dns-domain cluster.local \
 --upload-certs
```

echo 1 > /proc/sys/net/ipv4/ip_forward
由于默认拉取镜像地址k8s.gcr.io国内无法访问，这里指定阿里云镜像仓库地址。可以手动按照我们的images.sh先拉取镜像
地址变为registry.aliyuncs.com/google_containers 也可以

#### 2、测试kubectl（主节点执行）

```
  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

```

目前master状态为notready。等待网络加入完成即可

journalctl -u kubelet 查看kubelet日志

连接节点：
```
kubeadm join kuber4s.api:6443 --token xcjupd.k160i0z30e5w8csc \
    --discovery-token-ca-cert-hash sha256:2d29e7770c2d045fa361bac83520b2e08009e04a429543e0830cbe3049bfbcfb
    
kubeadm join 10.0.2.15:6443 --token zf3ph4.4zulf1lp6mfietyl \
    --discovery-token-ca-cert-hash sha256:b985a71e9669f7abad0f145318267ea6a50dcc8e9bd4638627ebbadbaed9d722

```

### 安装Pod网络插件（CNI）

kubectl apply f \
https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

使用资源中的kube-flannel.yml：
kubectl apply -f kube-flannel.yml

kubectl get pods -n kube-system 查看指定名称空间的pods
kubectl get pods --all-namespaces 查看指定所有名称空间的pods

执行watch kubectl get pod -n kube-system -o wide 监控pod进度


### 入门操作kubernetes集群

#### 1、部署一个tomcat

kubectl create deployment tomcat6 --image=tomcat:6.0.53-jre8
kubectl get pods -o wide 可以获取到tomcat信息

#### 2、暴露nginx访问

kubectl expose deployment tomcat6 --port=80 --target-port=8080 --type=NodePort 
Pod的80映射容器的8080；service会代理Pod的80

#### 3、动态扩容测试

kubectl get deployment

应用升级：kubectl set image（--help查看帮助）

扩容：kubectl scale --replicas=3 deployment tomcat6
扩容了多份，所以无论访问哪个node的指定端口，都可以访问到tomcat6

#### 4、删除

kubectl get all
kubectl delete deploy/nginx
kubectl delete service/nginx-service

流程：创建deployment会管理replicas,replicas控制pod数量，有pod故障会自动拉起新的pod

#### 5、Ingress（底层使用nginx）
通过Service发现Pod进行关联，基于域名访问。
通过Ingress Controller实现Pod负载均衡



## kubesphere（界面操作k8s）

文档：

[https://kubesphere.com.cn/docs/zh-CN/installation/install-on-k8s/](https://kubesphere.com.cn/docs/zh-CN/installation/install-on-k8s/)


### 1、安装helm（master节点执行）

Helm是Kubernetes的包管理器。

#### 1、安装

curl -L https://git.io/get_helm.sh | bash

集群架构篇资料中有get_helm.sh

#### 2、验证版本

helm version

#### 3、创建权限（master执行）

创建helm_rbac.yaml

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tiller
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: tiller
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
  - kind: ServiceAccount
    name: tiller
    namespace: kube-system

```

kubectl apply -f helm_rbac.yaml

### 2、安装Tiller（master执行）

#### 1、初始化

helm init --service-account=tiller --tiller-image=sapcc/tiller:v2.16.3 --history-max 300  

--tiller-image指定镜像，否则会被墙

等待节点上部署的tiller完成即可

#### 2、有默认的存储类型

- 已有 Kubernetes 集群，并安装了 kubectl 或 HelmPod 可以被调度到
- 集群的 master 节点（可临时取消 master 节点的 Taint）

关于第二个前提条件，是由于安装 OpenEBS 时它有一个初始化的 Pod 需要在 master 节点启动并创建 PV 给 KubeSphere 的有状态应用挂载。因此，若您的 master 节点存在 Taint，建议在安装 OpenEBS 之前手动取消 Taint，待 OpenEBS 与 KubeSphere 安装完成后，再对 master 打上 Taint，以下步骤供参考：


[https://kubesphere.com.cn/docs/zh-CN/appendix/install-openebs/](https://kubesphere.com.cn/docs/zh-CN/appendix/install-openebs/)

#### 3、安装

参照文档即可
[https://kubesphere.com.cn/docs/zh-CN/installation/install-on-k8s/](https://kubesphere.com.cn/docs/zh-CN/installation/install-on-k8s/)


#### 4、测试


http://192.168.56.103:30880/
admin
P@88w0rd

如果能登录单报server异常，看看有没有coredns

或者重新安装，按这个哥们的文章
https://segmentfault.com/a/1190000022146020
安装到6.4节，也就是安装StorageClass这里开始替代成我这篇笔记的安装OpenEBS，接着再按我笔记安装即可



## 开启devops

扩充除master主机的内存

kubectl edit cm -n kubesphere-system ks-installer

参考如下修改 ConfigMap
```
devops:
      enabled: True
      jenkinsMemoryLim: 2Gi
      jenkinsMemoryReq: 1500Mi
      jenkinsVolumeSize: 8Gi
      jenkinsJavaOpts_Xms: 512m
      jenkinsJavaOpts_Xmx: 512m
      jenkinsJavaOpts_MaxRAM: 2g
      sonarqube:
        enabled: True
```