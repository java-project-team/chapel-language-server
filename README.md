# Chapel Language Server
## Описание проекта
Языковой сервер для поддержки языка [Chapel](https://chapel-lang.org/). Сейчас есть поддержка автодополнения, hover, перехода к declaration и definition и подсветка.

Для разработки использовали реализацию [Language Server Protocol](https://microsoft.github.io/language-server-protocol/) на Java - [lsp4j](https://github.com/eclipse/lsp4j).

## Инструкция по сборке
TODO
1. В папке chapel-server нужно сделать build с помошью Gradle, запустив таску shadowJar
2. В основной папке нужно прописать npm install
3. Запустить Visual Studio Code в основной папке, скомпилировать и запустить

## Пример работы
![2](https://user-images.githubusercontent.com/64271239/176164852-36445682-d95c-465a-9173-7759a493ab98.gif)
![222](https://user-images.githubusercontent.com/64271239/176165206-240af9dc-2440-40f2-be0d-2c5a8681d069.gif)
