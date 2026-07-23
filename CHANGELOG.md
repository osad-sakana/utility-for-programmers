# Changelog

このプロジェクトのすべての注目すべき変更を記録します。

フォーマットは [Keep a Changelog](https://keepachangelog.com/ja/1.1.0/) に準拠し、
バージョニングは [Semantic Versioning](https://semver.org/lang/ja/) に従います。

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
