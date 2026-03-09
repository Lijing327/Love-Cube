# 贡献指南

首先，感谢你有兴趣为 `Love Cube` 项目做出贡献！下面是一些指南，希望能帮助你更好地参与项目。

## 📋 行为准则

请遵守我们的 [行为准则](CODE_OF_CONDUCT.md)。简言之：
- 尊重所有贡献者
- 进行建设性的讨论
- 禁止任何形式的歧视或骚扰

## 🐛 报告 Bug

在提交 Bug 报告之前，请搜索 [Issue 列表](https://github.com/yourusername/Love-Cube/issues)，确保该 Bug 还未被报告。

提交 Bug 报告时，请包括：

- **清晰的描述** - 用简洁明了的语言描述 Bug
- **重现步骤** - 提供具体的步骤来重现问题
- **预期行为** - 描述你认为应该发生什么
- **实际行为** - 描述实际发生了什么
- **截图或视频** - 如果可能，提供截图或视频
- **系统环息**：
  - 操作系统和版本
  - Java/Node.js 版本（如适用）
  - 浏览器版本或微信版本
  - 项目版本

### Bug 报告模板

```markdown
## 问题描述
[简化的描述]

## 重现步骤
1. [第一步]
2. [第二步]
3. ...

## 预期行为
[预期发生什么]

## 实际行为
[实际发生什么]

## 系统信息
- OS: [例如 Windows 10]
- Java: [例如 Java 17]
- 浏览器/小程序版本: [例如 WeChat 8.0.20]

## 截图
[如有，请粘贴]
```

## 💡 建议新功能

如果你有新功能的想法，欢迎提交 Issue 进行讨论。请包括：

- **功能描述** - 清楚地描述你想要的功能
- **使用场景** - 解释这个功能的用途
- **实现方案** - 如果有想法，可以分享实现方案
- **优先级** - 标记为 `enhancement`

## 🔀 贡献代码

### 1. Fork 项目

点击 [Fork](https://github.com/yourusername/Love-Cube/fork) 按钮，将项目 Fork 到你的账户。

### 2. 克隆仓库

```bash
git clone https://github.com/YOUR_USERNAME/Love-Cube.git
cd Love-Cube
git remote add upstream https://github.com/yourusername/Love-Cube.git
```

### 3. 创建功能分支

```bash
git checkout -b feature/your-feature-name
# 或者修复 bug
git checkout -b fix/bug-description
```

分支命名规范：
- `feature/` - 新功能
- `fix/` - Bug 修复
- `docs/` - 文档更新
- `test/` - 测试相关
- `refactor/` - 代码重构
- `chore/` - 构建、工具等调整

### 4. 开发和测试

#### 后端开发
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### 前端开发
```bash
cd frontend
# 在微信开发者工具中打开并预览
```

### 5. 运行测试

确保你的代码通过了所有测试：

```bash
cd backend
./mvnw clean test
```

### 6. 提交代码

遵循语义化提交信息规范：

```bash
git add .
git commit -m "type: description"
```

**类型**：
- `feat` - 新功能
- `fix` - Bug 修复
- `docs` - 文档
- `style` - 格式变化
- `refactor` - 代码重构
- `test` - 测试
- `chore` - 构建、配置等

**示例**：
```bash
git commit -m "feat: add user search by location"
git commit -m "fix: resolve null pointer exception in chat service"
git commit -m "docs: update deployment guide"
```

### 7. 推送到远程仓库

```bash
git push origin feature/your-feature-name
```

### 8. 提交 Pull Request

在 GitHub 上创建 Pull Request，请：

- 对比到 `upstream/main` 分支
- 提供清晰的 PR 标题和描述
- 关联相关的 Issue（使用 `#issue_number`）
- 添加适当的标签（`bug`, `feature`, `documentation` 等）

### PR 模板

```markdown
## 描述
[简短的改动说明]

## 相关 Issue
fixes #[issue number]

## 改动类型
- [ ] Bug 修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 代码重构

## 测试情况
- [ ] 已在本地测试
- [ ] 添加了新的测试用例
- [ ] 所有测试通过

## 检查清单
- [ ] 遵循代码规范
- [ ] 更新了相关文档
- [ ] 没有中断性改动
- [ ] 提交信息清晰
```

## 📝 代码规范

### Java 规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 使用 4 个空格缩进
- 类名：`PascalCase`
- 方法名：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 添加 JavaDoc 注释到公开接口

示例：
```java
/**
 * 用户搜索服务
 * 用于处理用户的搜索和发现功能
 */
public class UserSearchService {
    
    /**
     * 根据关键词搜索用户
     * 
     * @param keyword 搜索关键词
     * @param pageSize 分页大小
     * @return 用户列表
     */
    public List<UserDTO> searchByKeyword(String keyword, int pageSize) {
        // 实现代码
    }
}
```

### JavaScript 规范

- 使用分号结尾
- 4 个空格缩进
- 使用 `const` 和 `let`，避免 `var`
- 函数名：`camelCase`
- 常量：`UPPER_SNAKE_CASE`

示例：
```javascript
// 获取用户信息
const getUserInfo = async (userId) => {
  try {
    const response = await fetch(`/api/users/${userId}`);
    return await response.json();
  } catch (error) {
    console.error('Failed to fetch user info:', error);
    throw error;
  }
};
```

### SQL 规范

- 使用大写关键字：`SELECT`, `FROM`, `WHERE` 等
- 缩进 2 个空格
- 单行注释用 `--`，多行注释用 `/* */`

```sql
-- 创建用户表
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📚 文档贡献

改进文档也是很重要的贡献方式！

- 修复拼写或语法错误
- 澄清不清楚的说明
- 添加缺失的文档
- 改进示例代码

所有文档文件都应该用 Markdown 格式编写，并放在 `docs/` 或根目录。

## 🔍 代码审查流程

- 至少 1 个维护者会审查你的 PR
- 我们会提供反馈和建议
- 如果需要修改，请更新分支（不需要重新创建 PR）
- 一旦批准，我们会合并你的 PR

## ❓ 常见问题

### 我应该如何选择要贡献的内容？

查看 [Issue 列表](https://github.com/yourusername/Love-Cube/issues) 中标记为以下标签的任务：
- `good first issue` - 适合新手
- `help wanted` - 寻求帮助
- `enhancement` - 功能增强

### 我如何保持我的分支与 upstream 同步？

```bash
git fetch upstream
git rebase upstream/main
git push origin feature/your-feature-name -f
```

### 如何在本地运行完整的应用？

请参考 [QUICK_START.md](./QUICK_START.md)

## 📞 需要帮助？

- 在 GitHub 中提交 Issue
- 参与 [GitHub Discussions](https://github.com/yourusername/Love-Cube/discussions)
- 发送邮件至 contact@example.com

感谢你的贡献！🎉
