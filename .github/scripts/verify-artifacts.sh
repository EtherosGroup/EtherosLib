#!/usr/bin/env bash
# 构建产物校验与归置
#
# 校验项：jar 完整性、插件元数据版本与主类、common 与 EtherosFramework 的合并结果、字节码目标版本
# 通过后复制到 dist/ 并生成 SHA256SUMS.txt
set -euo pipefail

# 工程版本来源
version=$(sed -n 's/^projectVersion=\(.*\)$/\1/p' gradle.properties)
if [ -z "$version" ]; then
  echo "错误：projectVersion 缺失：gradle.properties"
  exit 1
fi

# 三个模块的产物路径
bukkit="bukkit/build/libs/EtherosLib-Bukkit-${version}.jar"
velocity="velocity/build/libs/EtherosLib-Velocity-${version}.jar"
common="common/build/libs/EtherosLib-Common-${version}.jar"

# jar 存在性与完整性
for jar in "$bukkit" "$velocity" "$common"; do
  if [ ! -f "$jar" ]; then
    echo "错误：产物缺失：$jar"
    exit 1
  fi
  if ! unzip -t "$jar" > /dev/null; then
    echo "错误：jar 损坏：$jar"
    exit 1
  fi
done

# Bukkit 插件元数据
if ! unzip -p "$bukkit" plugin.yml | grep -qx "version: '${version}'"; then
  echo "错误：plugin.yml 版本不匹配：期望 ${version}"
  exit 1
fi
if ! unzip -p "$bukkit" plugin.yml | grep -qx "main: cn.etheros.etheroslib.bukkit.EtherosLib"; then
  echo "错误：plugin.yml 主类不匹配：EtherosLib"
  exit 1
fi

# Velocity 插件元数据
if ! unzip -p "$velocity" velocity-plugin.json | grep -q "\"version\":\"${version}\""; then
  echo "错误：velocity-plugin.json 版本不匹配：期望 ${version}"
  exit 1
fi
if ! unzip -p "$velocity" velocity-plugin.json | grep -q '"main":"cn.etheros.etheroslib.velocity.EtherosLibVelocity"'; then
  echo "错误：velocity-plugin.json 主类不匹配：EtherosLibVelocity"
  exit 1
fi

# common 与 EtherosFramework 的合并结果
for jar in "$bukkit" "$velocity"; do
  if ! unzip -l "$jar" | grep -q "cn/etheros/etheroslib/common/EtherosLibCore.class"; then
    echo "错误：common 未合并：$(basename "$jar")"
    exit 1
  fi
  if ! unzip -l "$jar" | grep -q "cn/etheros/etherosframework/"; then
    echo "错误：EtherosFramework 未合并：$(basename "$jar")"
    exit 1
  fi
done

# class 文件主版本号
major_of() {
  unzip -p "$1" "$2" | od -An -tu1 -N8 | awk '{print $8}'
}

# Java 8 = 52，Java 17 = 61
if [ "$(major_of "$bukkit" cn/etheros/etheroslib/bukkit/EtherosLib.class)" != "52" ]; then
  echo "错误：Bukkit 字节码目标非 Java 8"
  exit 1
fi
if [ "$(major_of "$velocity" cn/etheros/etheroslib/velocity/EtherosLibVelocity.class)" != "61" ]; then
  echo "错误：Velocity 字节码目标非 Java 17"
  exit 1
fi
if [ "$(major_of "$common" cn/etheros/etheroslib/common/EtherosLibCore.class)" != "52" ]; then
  echo "错误：Common 字节码目标非 Java 8"
  exit 1
fi

# 归置发布产物
mkdir -p dist
cp "$bukkit" "$velocity" "$common" dist/
(cd dist && sha256sum ./*.jar > SHA256SUMS.txt)
ls -l dist
