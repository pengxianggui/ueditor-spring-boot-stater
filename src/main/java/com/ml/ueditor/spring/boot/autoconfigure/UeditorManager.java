package com.ml.ueditor.spring.boot.autoconfigure;

import com.ml.ueditor.spring.boot.autoconfigure.define.ActionMap;
import com.ml.ueditor.spring.boot.autoconfigure.manager.DefaultFileManager;
import com.ml.ueditor.spring.boot.autoconfigure.manager.IUeditorFileManager;
import com.ml.ueditor.spring.boot.autoconfigure.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Ueditor配置
 *
 * @author L.cm
 */
public class UeditorManager implements InitializingBean {
    private static final String CONFIG_FILE_JSON = "/ueditor.config.json";
    private UeditorConfig jsonConfig = null;

    private IUeditorFileManager fileManager;

    public UeditorManager() {
        fileManager = new DefaultFileManager();
    }

    public IUeditorFileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(IUeditorFileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try (InputStream input = UeditorManager.class.getResourceAsStream(CONFIG_FILE_JSON);) {
            Assert.notNull(input, "can't find ueditor.config.json");
            String configContent = IOUtils.toString(input, StandardCharsets.UTF_8);
            jsonConfig = JsonUtils.parse(filter(configContent), UeditorConfig.class);
        }
    }

    // 验证配置文件加载是否正确
    public boolean valid() {
        return jsonConfig != null;
    }

    public UeditorConfig getConfig() {
        return jsonConfig;
    }

    public ActionConfig getConfig(int type, String rootPath) {
        ActionConfig conf = new ActionConfig();
        String savePath = null;
        switch (type) {
            case ActionMap.UPLOAD_FILE:
                conf.setBase64(false);
                conf.setMaxSize(jsonConfig.getFileMaxSize());
                conf.setAllowFiles(jsonConfig.getFileAllowFiles());
                conf.setFieldName(jsonConfig.getFileFieldName());
                savePath = jsonConfig.getFilePathFormat();
                break;

            case ActionMap.UPLOAD_IMAGE:
                conf.setBase64(false);
                conf.setMaxSize(jsonConfig.getImageMaxSize());
                conf.setAllowFiles(jsonConfig.getImageAllowFiles());
                conf.setFieldName(jsonConfig.getImageFieldName());
                savePath = jsonConfig.getImagePathFormat();
                break;

            case ActionMap.UPLOAD_VIDEO:
                conf.setMaxSize(jsonConfig.getVideoMaxSize());
                conf.setAllowFiles(jsonConfig.getVideoAllowFiles());
                conf.setFieldName(jsonConfig.getVideoFieldName());
                savePath = jsonConfig.getVideoPathFormat();
                break;

            case ActionMap.UPLOAD_SCRAWL:
                conf.setFilename("scrawl");
                conf.setMaxSize(jsonConfig.getScrawlMaxSize());
                conf.setFieldName(jsonConfig.getScrawlFieldName());
                conf.setBase64(true);
                savePath = jsonConfig.getScrawlPathFormat();
                break;

            case ActionMap.CATCH_IMAGE:
                conf.setFilename("remote");
                conf.setFilter(jsonConfig.getCatcherLocalDomain());
                conf.setMaxSize(jsonConfig.getCatcherMaxSize());
                conf.setAllowFiles(jsonConfig.getCatcherAllowFiles());
                conf.setFieldName(jsonConfig.getCatcherFieldName() + "[]");
                savePath = jsonConfig.getCatcherPathFormat();
                break;

            case ActionMap.LIST_IMAGE:
                conf.setAllowFiles(jsonConfig.getImageManagerAllowFiles());
                conf.setDir(jsonConfig.getImageManagerListPath());
                conf.setCount(jsonConfig.getImageManagerListSize());
                break;

            case ActionMap.LIST_FILE:
                conf.setAllowFiles(jsonConfig.getFileManagerAllowFiles());
                conf.setDir(jsonConfig.getFileManagerListPath());
                conf.setCount(jsonConfig.getFileManagerListSize());
                break;
            default:
                break;
        }
        conf.setSavePath(savePath);
        conf.setRootPath(rootPath);
        return conf;
    }

    /**
     * 过滤json, 剔除多行注释以及替换掉反斜杠
     */
    private static String filter(String input) {
        return input.replaceAll("/\\*[\\s\\S]*?\\*/", "");
    }
}
