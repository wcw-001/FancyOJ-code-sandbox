package com.wcw.wojcodesandbox.security;

import java.security.Permission;

/**
 * 默认安全管理器
 */
public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm){
        System.out.println("checkPermission");
        System.out.println(perm);
        //super.checkPermission(perm);
    }

}
