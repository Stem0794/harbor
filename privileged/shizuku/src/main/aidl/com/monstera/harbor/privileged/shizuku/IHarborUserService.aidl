package com.monstera.harbor.privileged.shizuku;

interface IHarborUserService {
    void destroy() = 16777114;
    String diagnostics() = 1;
    String listUsers() = 2;
    String installExisting(String packageName, int userId) = 3;
    String createFullUser(String name) = 4;
    String installHarbor(String packageName, int userId) = 5;
    String switchUser(int userId) = 6;
    String listPackages(int userId) = 7;
    String currentUser() = 8;
}
