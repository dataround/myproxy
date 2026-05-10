@echo off
chcp 65001 >nul
title MyProxy Startup

:: 设置 MyProxy JAR 所在目录（修改为你的实际路径）
set "MYPROXY_DIR=d:\Devtools\myproxy"
set "JAR_NAME=myproxy-1.0.0.jar"

cd /d "%MYPROXY_DIR%"

:: 使用 javaw 后台运行（无控制台窗口）
start "" javaw -jar "target\%JAR_NAME%"
