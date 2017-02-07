package job;

import job.config.JobConfig;
import job.log.JobLogManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import reg.listener.ConnectListener;
import reg.listener.JobListener;
import reg.zookeeper.ZookeeperRegistryCenter;

import java.util.Date;

/**
 * 任务启动类
 * Created by alan.zheng on 2017/1/17.
 */
public abstract class JobExecutor {
    private final JobConfig jobConfig;
    private final ZookeeperRegistryCenter zookeeperRegistryCenter;
    public JobExecutor(JobConfig _jobConfig, ZookeeperRegistryCenter _zookeeperRegistryCenter){
        jobConfig=_jobConfig;
        zookeeperRegistryCenter=_zookeeperRegistryCenter;
    }
    /**
     * 任务启动入口方法
     */
    protected void excute(){
        /**
         * 开始前置处理，(开始进行zk监听处理)
         */
        //启动任务时候插入zk临时任务数据
        if (!zookeeperRegistryCenter.isExisted("/"+jobConfig.getJobName()+"")){
            CuratorFramework curatorFramework=(CuratorFramework) zookeeperRegistryCenter.getRawClient();

            curatorFramework.getConnectionStateListenable().addListener(new ConnectListener());

            TreeCache treeCache=new TreeCache(curatorFramework,"/"+jobConfig.getJobName()+"");
            treeCache.getListenable().addListener(new JobListener(jobConfig.getJobName()));
            try {
                treeCache.start();
            } catch (Exception e) {
                JobLogManager.log(jobConfig.getJobName(),e.toString(),new Date());
            }
            zookeeperRegistryCenter.createEphemeral("/"+jobConfig.getJobName()+"",jobConfig.getJobName());
        }
        process();
    }

    /**
     * 任务业务程序处理
     */
    protected abstract void process();
}