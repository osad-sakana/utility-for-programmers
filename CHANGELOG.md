# Changelog

このプロジェクトのすべての注目すべき変更を記録します。

フォーマットは [Keep a Changelog](https://keepachangelog.com/ja/1.1.0/) に準拠し、
バージョニングは [Semantic Versioning](https://semver.org/lang/ja/) に従います。

## [2.0.0] - 2026-09-02

MOD ローダーを NeoForge から Fabric へ完全移行（破壊的変更）。Minecraft バージョンは 26.2 のまま。

### Changed
- MOD ローダーを NeoForge 26.2.0.28-beta → **Fabric Loader 0.19.3**（+ Fabric API）に変更。
- ビルドシステムを `net.neoforged.moddev` Gradle プラグインから **Fabric Loom**（`net.fabricmc.fabric-loom`）に変更。
- `net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent` を Fabric API の `LevelRenderEvents.COLLECT_SUBMITS`（`LevelRenderContext`）に置き換え。
- `RegisterKeyMappingsEvent` → `KeyMappingHelper.registerKeyMapping`、`RegisterGuiLayersEvent`/`GuiLayer` → `HudElementRegistry`/`HudElement`、`ClientTickEvent.Post` → `ClientTickEvents.END_CLIENT_TICK` に置き換え。
- 設定ファイルを `config/utilitiesforprogrammers-client.toml`（NeoForge `ModConfigSpec`）から **`config/utilitiesforprogrammers-client.json`**（自前実装、1秒間隔のファイル変更検知でホットリロード）に変更。
- `neoforge.mods.toml` を `fabric.mod.json` に置き換え。

### Removed
- NeoForge のサポートを終了。既存の NeoForge 版（1.1.0 以前）は今後アップデートされません。

## [1.1.0] - 2026-07-23

Minecraft 26.2（Chaos Cubed）/ NeoForge 26.2.0.28-beta 対応。

### Changed
- ビルド対象を Minecraft 1.21.10 → 26.2、NeoForge 21.10.64 → 26.2.0.28-beta、Java 21 → 25 に更新。
- `ResourceLocation` → `Identifier`、`GuiGraphics` → `GuiGraphicsExtractor` などの API リネームに追従。
- ワールド空間の描画（ハイライト枠・塗りつぶし・座標軸グリッド）を、廃止された `MultiBufferSource` / `ShapeRenderer` から新しい `SubmitCustomGeometryEvent` ベースの描画パイプラインに移行。
- `neoforge.mods.toml` をビルド時テンプレート展開方式（`src/main/templates/`）に変更。

## [1.0.0] - 2026-06-26

初回リリース。Minecraft 1.21.10 / NeoForge 21.10.x 向けのクライアントサイド専用 MOD。

### Added
- **HUD** — 絶対座標・向いている方角・視線の先のブロック情報（ブロックID＋ブロックステート）を画面左上に表示。
- **ブロック更新ハイライト** — サーバーから届くブロック更新を配置順に色分け（新しい＝赤 → 古い＝青）し、時間でフェードアウト。client packet listener への mixin で検知するため、バニラ（MOD 無し）サーバーでも動作。
- **ターゲットハイライト** — 視線の先のブロックを強調する枠線＋半透明フィル。
- **座標軸グリッド** — プレイヤー周囲の1ブロックグリッドと、原点 (0,0,0) を基準とした座標軸の矢印（+X = 赤 / +Z = 青）を Y=0 平面に固定描画。
- **ウィンドウ操作** — 非フォーカス時の描画継続、画面端のフォーカス枠、常に最前面、外部操作モード。
- **操作キー** — `H` で全機能を一括 ON/OFF、`K` で外部操作モード（マウス解放＋移動停止＋クリック無効化＋HUD/ハイライトの静止）。
- **設定** — `config/utilitiesforprogrammers-client.toml` で各機能・色・表示時間などを再起動なしで調整可能。

[1.1.0]: https://github.com/osad-sakana/utilitiesforprogrammers/releases/tag/v1.1.0
[1.0.0]: https://github.com/osad-sakana/utilitiesforprogrammers/releases/tag/v1.0.0
