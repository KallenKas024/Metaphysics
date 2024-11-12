package com.example.cryptography;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Database implements ILuaAPI {

    public static Map<String, Connection> ComputerConnectionMapper = new HashMap<>();
    private int id;
    private ServerLevel level;

    public Database(int id, ServerLevel serverLevel) {
        this.id = id;
        this.level = serverLevel;
    }

    @Override
    public String[] getNames() {
        return new String[]{"db", "database"};
    }

    @LuaFunction
    public final @NotNull String establish(String path) {
        String directoryPath = this.level.getServer().getWorldPath(LevelResource.ROOT) +"/computercraft/computer/" + id + "/Database/";
        
        Path directory = Paths.get(directoryPath);
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory); 
                System.out.println("目录创建成功：" + directory);
            } else {
                System.out.println("目录已存在：" + directory);
            }
        } catch (IOException e) {
            System.out.println("创建目录时发生错误：" + e.getMessage());
        }
        if (!Files.exists(Path.of(directoryPath + path))) {
            try {
                Files.createFile(Path.of(directoryPath + path));
            } catch (Exception e) {}
        }
        if (!path.contains("..") && !path.contains("/") && !path.contains("~")) {
            String code = null;
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ this.level.getServer().getWorldPath(LevelResource.ROOT) +"/computercraft/computer/" + id + "/Database/" + path);
                code = "EST" + UUID.randomUUID();
                ComputerConnectionMapper.put(code, connection);
            } catch (SQLException ignored) {
            }
            return code;
        } else {
            return "false value";
        }
    }

    @LuaFunction
    public final String CustomLinker(String Link, String User, String Password) {
        String url = Link;
        String user = User;
        String password = Password;
        String regex = "^jdbc:[^:]+://.*?(:\\d+)?/[^?]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        boolean matches = matcher.matches();
        if (!matches) {
            return "false value";
        }
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            String code = "EST" + UUID.randomUUID();
            ComputerConnectionMapper.put(code, conn);
            return code;
        } catch (SQLException ignored) {
            return "false value";
        }
    }

    @LuaFunction
    public final boolean prepareStatement(String instance, String sql) {
        Connection connection = ComputerConnectionMapper.get(instance);
        try {
            connection.prepareStatement(sql);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @LuaFunction
    public final boolean createStatement(String instance) {
        Connection connection = ComputerConnectionMapper.get(instance);
        try {
            connection.createStatement();
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @LuaFunction
    public final boolean executeUpdate(String instance, String sql) {
        Connection connection = ComputerConnectionMapper.get(instance);
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @LuaFunction
    public final boolean execute(String instance, String sql) {
        Connection connection = ComputerConnectionMapper.get(instance);
        try {
            connection.createStatement().execute(sql);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @LuaFunction
    public final Map<String, Object> executeQuery(String instance, String sql) {
        Connection connection = ComputerConnectionMapper.get(instance);
        try {
            ResultSet res = connection.createStatement().executeQuery(sql);
            Map<String, Object> result = new HashMap<>();
            ResultSetMetaData metaData = res.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = res.getObject(i);
                result.put(columnName, columnValue);
            }
            return result;

        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    @LuaFunction
    public final boolean close(String instance) {
        Connection connection = ComputerConnectionMapper.get(instance);
        try {
            connection.close();
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
}
