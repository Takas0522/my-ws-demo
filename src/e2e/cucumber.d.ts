/**
 * Cucumber用の型定義ファイル
 * TypeScriptでCucumberを使用する際に必要な型定義を提供
 */

declare module '@cucumber/cucumber' {
  export interface World {
    // Worldオブジェクトに追加のプロパティを定義できます
    [key: string]: any;
  }

  export function Given(pattern: string | RegExp, code: (...args: any[]) => void | Promise<void>): void;
  export function When(pattern: string | RegExp, code: (...args: any[]) => void | Promise<void>): void;
  export function Then(pattern: string | RegExp, code: (...args: any[]) => void | Promise<void>): void;
  export function Before(code: (this: World) => void | Promise<void>): void;
  export function Before(options: any, code: (this: World) => void | Promise<void>): void;
  export function After(code: (this: World) => void | Promise<void>): void;
  export function After(options: any, code: (this: World) => void | Promise<void>): void;
  export function BeforeAll(code: () => void | Promise<void>): void;
  export function AfterAll(code: () => void | Promise<void>): void;
  export function setDefaultTimeout(timeout: number): void;
  export function setWorldConstructor(constructor: any): void;
}
