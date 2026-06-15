# NashiPomodoro

[简体中文](README_ZH.md) | [English](README.md) | [日本語](README_JA.md)

NashiPomodoro は Nothing Phone 向けの Android ポモドーロタイマーです。集中タイマー、タスク管理、統計機能を Glyph Interface と組み合わせ、端末背面のライトで集中時間と休憩時間の進捗を視覚的に表示します。

## 重要：システムの Glyph 進捗表示を無効にしてください

> [!IMPORTANT]
> Nothing Phone で本アプリを使用する前に、Nothing OS のシステム設定で標準の **Glyph 進捗表示（Glyph Progress）** を必ず無効にしてください。
>
> システム標準の進捗表示が Glyph Interface を使用すると、NashiPomodoro のライト制御と競合します。その結果、本アプリの進捗ライトが表示されない、正しく表示されない、またはタイマーと Glyph エフェクトが正常に連動しない場合があります。

メニュー名は Nothing OS のバージョンによって多少異なる場合があります。システムの **Glyph Interface** 設定を開き、標準の進捗表示または進捗追跡機能を無効にしてから NashiPomodoro を起動してください。

## 機能

- 集中、短い休憩、長い休憩の時間を設定可能
- ポモドーロの各フェーズを自動で切り替え、長い休憩の間隔も設定可能
- Glyph ライトで集中、短い休憩、長い休憩の残り時間を表示

## 動作要件

- Android 14（API 34）以降
- 対応する Glyph ライトを搭載した Nothing Phone を推奨
- 通知、バイブレーション、Glyph Interface に関する権限

Android のバージョン要件を満たす他の端末でも基本的なポモドーロ機能は使用できますが、Glyph エフェクトはコード上で対応している Nothing Phone でのみ有効になります。

## 開発環境

- Android Studio
- JDK 21
- Android SDK 36.1
- Gradle 9.5

## 権限について

- **通知**：タイマー実行中のフォアグラウンドサービス通知を表示します。
- **フォアグラウンドサービスと Wake Lock**：アプリがバックグラウンドに移動してもタイマーを安定して動作させます。
- **バイブレーション**：集中または休憩フェーズの終了時に触覚フィードバックを提供します。
- **Glyph**：対応する Nothing Phone の Glyph Interface エフェクトを制御します。

## 翻訳

本プロジェクトでは標準の Android 文字列リソースを使用して多言語化しています。翻訳の追加や更新については [TRANSLATING.md](TRANSLATING.md) を参照してください。
