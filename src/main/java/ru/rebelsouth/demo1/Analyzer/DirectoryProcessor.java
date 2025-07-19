package ru.rebelsouth.demo1.Analyzer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryProcessor {
    private final Path rootPath;
    private final int DEPTH;

    public DirectoryProcessor(Path rootPath, int depth) {
        if (rootPath == null) {
            throw new IllegalArgumentException("Корневая папка не может быть null");
        }
        if (depth <= 0) {
            throw new IllegalArgumentException("Глубина должна быть положительная");
        }
        this.rootPath = rootPath;
        this.DEPTH = depth;
    }

    public void cleanFileSystem(PrefixTree root) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
//                Files.isRegularFile(file);
//                if (Files.isRegularFile(file))
//                file.getFileName().toString().endsWith(".txt")
//                    System.out.println("Файл " + file.getFileName() + " является обычным файлом");
//                System.out.println("Файл является обычным файлом " + Files.isRegularFile(file));
//                if (file.getFileName().toString().endsWith(".jpg") || file.getFileName().toString().endsWith(".png")) {
//                    System.out.println("Файл " + file.getFileName() + " является картинкой");
//                }
                if (Files.isRegularFile(file)) {
                    processFile(file, root);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                cleanEmptyDir(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void processFile(Path file, PrefixTree root) {
        try {
            Path parent = file.getParent();
            String number = pathToNumber(parent);

            if (!root.contains(number)) {
                Files.deleteIfExists(file);
                System.out.println("Удален: " + file);
            }
        } catch (IOException e) {
            System.err.println("Ошибка с файлом: " + file);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Пропускаем файл: " + file);
        }
    }

    public String pathToNumber(Path path) {
        Path relative = rootPath.relativize(path);
        if (relative.getNameCount() < DEPTH) {
            throw new IllegalArgumentException("Глубина меньше, чем указано");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DEPTH; i++) {
            sb.append(relative.getName(i).toString());
        }
        return sb.toString();
    }

    private void cleanEmptyDir(Path dir) {
        try {
            if (!dir.equals(rootPath) &&
                    Files.list(dir).count() == 0) {
                Files.delete(dir);
                System.out.println("Удалена пустая директория: " + dir);
            }
        } catch (IOException e) {
            System.err.println("Ошибка IO: " + dir);
            e.printStackTrace();
        }
    }
}

