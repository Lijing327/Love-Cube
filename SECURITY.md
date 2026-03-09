# 安全政策

## 支持的版本

| 版本 | 状态 | 支持到期 |
| --- | --- | --- |
| 1.0.x | ✅ 支持中 | 2026-12-31 |
| 0.x | ⚠️ 停止支持 | 2024-06-30 |

## 安全漏洞报告

### 如何报告安全漏洞

**请不要通过公共 Issue 报告安全漏洞。** 安全漏洞应该私下报告。

请通过以下方式报告：

1. **GitHub 私密安全公告** (推荐)
   - 访问项目的 [Security Advisory](https://github.com/yourusername/Love-Cube/security/advisories)
   - 点击 "Report a vulnerability"
   - 填写漏洞详情

2. **电子邮件**
   - 发送至：security@example.com
   - 使用 GPG 加密（公钥见下文）
   - 包括：
     - 漏洞描述
     - 受影响的版本
     - 重现步骤（如果可能）
     - 潜在影响
     - 建议的修复方案（如果有）

3. **保密联系**
   - 维护者：contact@example.com
   - 响应时间：24 小时内

### 公告 GPG 密钥

```
-----BEGIN PGP PUBLIC KEY BLOCK-----
[公钥内容]
-----END PGP PUBLIC KEY BLOCK-----
```

## 漏洞处理流程

1. **初步确认** (1-2 天)
   - 确认漏洞的有效性
   - 确定受影响的版本

2. **修复开发** (2-7 天)
   - 在私有分支中开发修复
   - 编写测试用例
   - 确保修复不引入新的漏洞

3. **安全公告** (同时发布)
   - 发布修补版本
   - 发布 CVE 公告（如适用）
   - 通知受影响用户

4. **公开披露** (修复发布后)
   - GitHub Advisory 公开
   - 详细的漏洞说明
   - 感谢安全研究人员

## 已知的安全承诺

我们致力于维护以下安全标准：

### 依赖管理
- ✅ 定期更新依赖库
- ✅ 检查 CVE 漏洞
- ✅ 及时修复高危漏洞
- ✅ 维护依赖清单（SBOM）

### 代码安全
- ✅ 代码审查（Pull Request 审查）
- ✅ 静态代码分析（SAST）
- ✅ 依赖扫描（SCA）
- ✅ 遵循安全编码规范

### 认证与授权
- ✅ 使用 JWT 令牌认证
- ✅ 密码加密存储（bcrypt）
- ✅ CORS 安全配置
- ✅ SQL 注入防护（参数化查询）

### 数据保护
- ✅ HTTPS 强制（生产环境）
- ✅ 敏感信息不在日志中
- ✅ 数据库连接加密
- ✅ 上传文件检查

## 安全更新

### 关键安全更新
- 立即发布补丁版本
- 包含在下一个主要版本中
- 公开报告（CVE）

### 重要安全更新
- 在下一个次版本中发布
- 也可作为补丁版本发布
- 通知订阅用户

### 一般安全更新
- 包含在定期发布中
- 在更新日志中记录

## 依赖脆弱性

我们使用以下工具检查依赖：

### 后端（Java/Maven）
- OWASP Dependency-Check
- Snyk
- GitHub Dependabot

### 前端（JavaScript）
- npm audit
- Snyk
- GitHub Dependabot

## 测试策略

- 单元测试覆盖关键安全功能
- 集成测试验证安全配置
- 手动渗透测试（定期）
- 边界值测试和错误处理

## 安全最佳实践

对于使用此项目的用户：

1. **保持更新**
   - 定期检查更新
   - 订阅安全公告
   - 及时部署补丁

2. **安全配置**
   - 不要在代码中硬编码密钥
   - 使用环境变量存储敏感信息
   - 启用 HTTPS
   - 配置防火墙规则

3. **监控和日志**
   - 启用安全审计日志
   - 监控异常活动
   - 定期检查日志

4. **数据保护**
   - 备份数据库
   - 使用加密存储敏感数据
   - 限制数据库访问权限

## 相关资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CWE/SANS Top 25](https://cwe.mitre.org/top25/2023/)
- [Spring Security 文档](https://spring.io/projects/spring-security)
- [NIST 网络安全框架](https://www.nist.gov/cyberframework)

## 联系方式

- 安全问题：security@example.com
- 普通问题：contact@example.com
- GitHub Issues：[项目 Issue 页面](https://github.com/yourusername/Love-Cube/issues)

---

感谢安全研究人员帮助我们保持项目安全！
