package com.example.fastdfs.service;

import com.github.tobato.fastdfs.domain.conn.FdfsConnectionManager;
import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorageNodeInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.domain.proto.storage.StorageDownloadCommand;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.github.tobato.fastdfs.service.TrackerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * 基本文件存储客户端操作
 *
 * @date 2025/10/23
 */
@Service
@Slf4j
public class FileService {

    @Resource
    protected TrackerClient trackerClient;

    /**
     * connectManager
     */
    @Resource
    protected FdfsConnectionManager fdfsConnectionManager;

    @Resource
    protected FastFileStorageClient storageClient;


    // 上传
    public StorePath uploadFile(String groupName, InputStream inputStream, long fileSize, String fileExtName) {
//        StorageNode client = trackerClient.getStoreStorage(groupName);
//        StorageUploadFileCommand command = new StorageUploadFileCommand(client.getStoreIndex(), inputStream,
//                fileExtName, fileSize, false);
//        return fdfsConnectionManager.executeFdfsCmd(client.getInetSocketAddress(), command);

        log.info("##上传文件..##");
        StorePath path = storageClient.uploadFile(groupName, inputStream,
                fileSize, fileExtName);
        log.info("上传文件 result={}", path);

        log.info("##查询文件信息..##");
        FileInfo fileInfo = storageClient.queryFileInfo(path.getGroup(), path.getPath());
        log.info("查询文件信息 result={}", fileInfo);

        /*log.info("##下载文件..##");
        DownloadByteArray callback = new DownloadByteArray();
        // 下载部分文件
        byte[] content = storageClient.downloadFile(path.getGroup(),
                path.getPath(), 2, 8, callback);

        // 源文件当中截取
        byte[] subContent = new byte[8];
        System.arraycopy(file.toByte(), 2, subContent, 0, 8);

        log.info("##删除主文件..##");
        storageClient.deleteFile(path.getGroup(), path.getPath());*/

        return path;
    }

    public void delete(String groupName, String path) {
        storageClient.deleteFile(groupName, path);
    }


    public String queryFileInfo(String groupName, String path) {
        FileInfo fileInfo = storageClient.queryFileInfo(groupName, path);
        return fileInfo.toString();
    }


    public <T> T downloadFile(String groupName, String path, DownloadCallback<T> callback) {
        long fileOffset = 0;
        long fileSize = 0;
        return downloadFile(groupName, path, fileOffset, fileSize, callback);
    }

    public <T> T downloadFile(String groupName, String path, long fileOffset, long fileSize,
                              DownloadCallback<T> callback) {
        StorageNodeInfo client = trackerClient.getFetchStorage(groupName, path);
        StorageDownloadCommand<T> command = new StorageDownloadCommand<T>(groupName, path, fileOffset, fileSize, callback);
        return fdfsConnectionManager.executeFdfsCmd(client.getInetSocketAddress(), command);
    }
}
