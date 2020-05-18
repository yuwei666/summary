#### 主要

```
git add . 				添加文件到暂存区

git commit;				提交版本

git commit –amend		撤销上一次提交,并将暂存区文件重新提交

git branch <name>;		新建分支
git checkout <name>;	检出分支
git checkout -b <name>;	新建分支并检出

git branch -d <names>	删除分支 用空格分隔

//合并操作时，如果当前分支为待合并分支的历史版本，那么当前分支的引用指向待合并分支的指针即可。
git merge <name>		合并分支，当前的分支会合并name分支,并生成一个包含两个分支改变的新版本号
git rebase <name>		合并分支，实际上就是取出一系列的提交记录，“复制”他们，在另一个地方逐个放下去。rebase会更线性的提交历史。
git rebase <name1> <name2> 合并分支，将name2合并到name1分支，name2有序排列到name1下面

//HEAD 是一个对当前检出记录的符号引用 —— 也就是指向你正在其基础上进行工作的提交记录。
git checkout <hashCode> 检出某个版本号（根据哈希值），检出时只需要输入版本号哈希值前几位即可
git log 				查看提交记录的哈希值
git checkout <name>^	检出某个分支的上一个版本
git checkout <name>^2	检出某个分支的第二条父分支的上个版本
git branch -f <name> HEAD~3	将name分支强制指向HEAD的第3父级，并提交

//版本回退
git reset HEAD~1		分支回退到前一个记录（本地记录也回退，但远程分支无效）
git revert HEAD~1		复制前一个版本状态，并生成新的版本并提交

//自由修改提交
git cherry-pick	<提交号> 将一个或多个提交复制到当前版本（HEAD）下面，和rebase有些类似，但是更灵活
git rebase -i HEAD~3 	利用可视化窗口，对包括HEAD在内的前三个版本进行删除，排序，合并操作

git tag <name> <提交号>  创建标签，标签在代码库中起着“锚点”的作用

/*
<ref> 可以是任何能被 Git 识别成提交记录的引用，如果你没有指定的话，Git 会以你目前所检出的位置（HEAD）
*/
git bisect <ref>		查找哪一次代码提交引入了错误
	输出结果： <tag>_<numCommits>_g<hash>
	tag 表示的是离 ref 最近的标签， numCommits 是表示这个 ref 与 tag 相差有多少个提交记录， hash 表示的是你所给定的 ref 所表示的提交记录哈希值的前几位。
	当 ref 提交记录上有某个标签时，则只输出标签名称
```

#### 远程相关

```
/*
当克隆时会在本地仓库多一个名称origin/master的远程分支，远程分支反映了远程仓库(在你上次和它通信时)的状态。
远程分支有一个特别的属性，检出时自动进入分离 HEAD 状态。因为你不能直接在远程分支上进行操作，而是在其他地方完成工作后再进行合并等操作。
大多数远程仓库名称为origin，因为克隆时自动把远程仓库命名origin。
*/
git clone			

/*
1.从远程仓库下载本地仓库中缺失的提交记录
2.更新远程分支指针(如 origin/master)
实际上将本地仓库中的远程分支更新成了远程仓库相应分支最新的状态，但是并不会改变本地仓库(master)的状态。
*/
git fatch		
git fatch <remote> <place>	拉取远程分支
git push <remote> <source>:<destination> 指定拉取的来源和去向

git pull				等价于git fetch;git merge oringin/master(非线性)
git push <remote> <place>	
/*
例如:git pull origin master:foo
如果本地仓库没有foo，它先在本地创建了一个叫 foo 的分支，从远程仓库中的 master 分支中下载提交记录，并合并到 foo，然后再 merge 到我们的当前检出的分支。
*/
git pull origin <source>:<destination>

git pull --rebase 		等价于git fetch;git rebase oringin/master(线性)



/*
git push 或 git fetch 时不指定任何 source，方法就是仅保留冒号和 destination 部分，source 部分留空。 例如：git push origin :side
如果 push 空 <source> 到远程仓库，它会删除远程仓库中的分支。
如果 fetch 空 <source> 到本地，会在本地创建一个新分支。
*/
git push				将变更上传到远程仓库
git push <remote> <place>	指定push参数 <remote>:如origin 	<place>：place指定提交的来源和去向
git push origin <source>:<destination>	分别指定来源和去向



//设置远程分支origin/master 
git checkout -b foo o/master	通过远程分支检出一支新的分支
git branch -u origin/master foo	设置远程分支跟踪

```



如果你是在一个大的合作团队中工作, 很可能是master被锁定了, 需要一些Pull Request流程来合并修改。

> "Pull Request 是一种通知机制。你修改了他人的代码，将你的修改通知原来的作者，希望他合并你的修改，这就是 Pull Request。"

如果你直接提交(commit)到本地master, 然后试图推送(push)修改, 你将会收到这样类似的信息:

```
! [远程服务器拒绝] master -> master (TF402455: 不允许推送(push)这个分支; 你必须使用pull request来更新这个分支.)
```

远程服务器拒绝直接推送(push)提交到master, 因为策略配置要求 pull requests 来提交更新.

你应该按照流程,新建一个分支, 推送(push)这个分支并申请pull request,但是你忘记并直接提交给了master.现在你卡住并且无法推送你的更新.

新建一个分支feature, 推送到远程服务器. 然后reset你的master分支和远程服务器保持一致, 否则下次你pull并且他人的提交和你冲突的时候就会有问题.

```
git reset origin/master					版本号回退到与origin/master相同，本地版本号会被保留
git checkout -b feature [本地版本号]		在本地版本创建featrue分支
git push origin feature					推送feature
```



Rebase

+ 优点：使你的提交树变得很干净, 所有的提交都在一条线上
+ 缺点：修改了提交树的历史



