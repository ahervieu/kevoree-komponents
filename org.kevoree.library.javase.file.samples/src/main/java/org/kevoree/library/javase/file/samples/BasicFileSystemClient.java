package org.kevoree.library.javase.file.samples;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.javase.fileSystem.api.FileService;
import org.kevoree.log.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/08/13
 * Time: 10:30
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "test")
@Requires({
        @RequiredPort(name = "files", type = PortType.SERVICE, className = FileService.class, needCheckDependency = true)
})
@ComponentType
public class BasicFileSystemClient extends AbstractComponentType implements FileService {

    @Start
    public void start() {
        new Thread() {
            @Override
            public void run() {
                Set<String> filters = new HashSet<String>();
                filters.add("*.txt");
                if (list().length == 0 && listFromFilter(filters).length == 0) {
                    if (saveFile("/dummy.txt", new byte[0]) && getFileContent("/dummy.txt").length == 0) {
                        if (list().length == 1 && listFromFilter(filters).length == 0) {
                            filters.clear();
                            filters.add("*.txt");
                            if (listFromFilter(filters).length == 1) {
                                try {
                                    byte[] bytes = "First file created".getBytes("UTF-8");
                                    if (saveFile("/first.txt", bytes) && getFileContent("/first.txt").length == bytes.length) {
                                        filters.clear();
                                        filters.add("dummy.txt");
                                        if (list().length == 2 && listFromFilter(filters).length == 1) {
                                            if (delete("/first.txt") && getFileContent("/first.txt").length == 0) {
                                                if (mkdirs("/firstFolder/")) {
                                                    if (saveFile("/firstFolder/first.txt", bytes) && getFileContent("/firstFolder/first.txt").length == bytes.length) {
                                                        if (delete("/firstFolder/first.txt") && getFileContent("/firstFolder/first.txt").length == bytes.length) {
                                                            if (move("/first.txt", "/firstFolder/dummy.txt") && !delete("/first.txt")) {
                                                                if (delete("/firstFolder/")) {
                                                                    Log.info("{}: Test on FileSystem done. Everything seems to be fine", getName());
                                                                } else {
                                                                    Log.error("{}: Unable to delete the folder: /firstFolder/", getName());
                                                                }
                                                            } else {
                                                                Log.error("{}: Unable to move the file: /first.txt to /firstFolder/first.txt", getName());
                                                            }
                                                        } else {
                                                            Log.error("{}: Unable to delete the file: /firstFolder/first.txt", getName());
                                                        }
                                                    } else {
                                                        Log.error("{}: Unable to save the file: /firstFolder/first.txt", getName());
                                                    }
                                                } else {
                                                    Log.error("{}: Unable to create the folder: /firstFolder/", getName());
                                                }
                                            } else {
                                                Log.error("{}: Unable to delete the file: /first.txt", getName());
                                            }
                                        } else {
                                            Log.error("{}: Unable to list the files: /dummy.txt and first.txt", getName());
                                        }
                                    } else {
                                        Log.error("{}: Unable to create the file: /first.txt", getName());
                                    }
                                } catch (UnsupportedEncodingException ignored) {
                                }
                            } else {
                                Log.error("{}: Unable to list using filters the file: /dummy.txt", getName());
                            }
                        } else {
                            Log.error("{}: Unable to list the file: /dummy.txt", getName());
                        }
                    } else {
                        Log.error("{}: Unable to create the empty file: /dummy.txt", getName());
                    }
                } else {
                    Log.error("{}: Unable to list the file: /first.txt", getName());
                }
            }
        }.start();
    }


    @Override
    public String[] list() {
        if (isPortBinded("files")) {
            return getPortByName("files", FileService.class).list();
        } else {
            return new String[0];
        }
    }

    @Override
    public String[] listFromFilter(Set<String> filters) {
        if (isPortBinded("files")) {
            return getPortByName("files", FileService.class).listFromFilter(filters);
        } else {
            return new String[0];
        }
    }

    @Override
    public byte[] getFileContent(String relativePath) {
        if (isPortBinded("files")) {
            return getPortByName("files", FileService.class).getFileContent(relativePath);
        } else {
            return new byte[0];
        }
    }

    @Override
    public boolean saveFile(String relativePath, byte[] data) {
        return isPortBinded("files") && getPortByName("files", FileService.class).saveFile(relativePath, data);
    }

    @Override
    public boolean mkdirs(String relativePath) {
        return isPortBinded("files") && getPortByName("files", FileService.class).mkdirs(relativePath);
    }

    @Override
    public boolean delete(String relativePath) {
        return isPortBinded("files") && getPortByName("files", FileService.class).delete(relativePath);
    }

    @Override
    public boolean move(String oldRelativePath, String newRelativePath) {
        return isPortBinded("files") && getPortByName("files", FileService.class).move(oldRelativePath, newRelativePath);
    }
}
