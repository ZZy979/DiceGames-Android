# 新增游戏类型checklist
* [GameType](app/src/main/java/com/zzy/dicegames/common/GameType.java) 新增枚举值
* 游戏逻辑
  * [res/layout](app/src/main/res/layout) 实现游戏界面布局
  * [ui.game](app/src/main/java/com/zzy/dicegames/ui/game) 实现游戏Fragment和ViewModel
  * [BaseGameFragment](app/src/main/java/com/zzy/dicegames/ui/game/BaseGameFragment.java) 注册新游戏类型
* 得分
  * [data.entity](app/src/main/java/com/zzy/dicegames/data/entity) 新增得分实体类
  * [data.dao](app/src/main/java/com/zzy/dicegames/data/dao) 新增得分DAO接口
  * [ScoreDatabase](app/src/main/java/com/zzy/dicegames/data/ScoreDatabase.java) 注册实体类和DAO，更新版本号和migration
  * [utils.score](app/src/main/java/com/zzy/dicegames/utils/score) 导入/导出得分增加新类型
  * [schemas](app/schemas) 新增数据库schema（自动生成）
* 统计数据
  * [data.entity](app/src/main/java/com/zzy/dicegames/data/entity) 新增统计数据实体类
  * [res/layout](app/src/main/res/layout) 实现统计数据界面布局
  * [ui.stats](app/src/main/java/com/zzy/dicegames/ui/stats) 实现统计数据Fragment
* [test](app/src/test) 增加单元测试
* [assets/help](app/src/main/assets/help) 增加帮助文件
* [app/build.gradle](app/build.gradle) 更新版本号（可选）
* [screenshots](screenshots) 增加屏幕截图
* [README.md](README.md) 更新README
