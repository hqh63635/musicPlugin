module.exports = {
    /**
     * tab缩进大小,默认为2
     */
    tabWidth: 4,
    /**
     * 不使用tab
     */
    useTabs: false,
    /**
     * 末尾使用分号, 默认true
     */
    semi: false,
    /**
     * 使用单引号, 默认false(在jsx中配置无效, 默认都是双引号)
     */
    singleQuote: true,
    /** JSX标签闭合位置 默认false
     * false: <div
     *          className=""
     *          style={{}}
     *       >
     * true: <div
     *          className=""
     *          style={{}} >
     */
    jsxBracketSameLine: false,
    /**
     * 每行最多多少个字符换行默认80
     */
    printWidth: 100,
    /**
     * 要求对象字面量属性是否使用引号包裹,(‘as-needed’: 没有特殊要求，禁止使用，'consistent': 	* 保持一致 , preserve: 不限制，想用就用)
     */
    quoteProps: 'as-needed',
    /**
     * jsx 语法中使用单引号
     */
    jsxSingleQuote: false,
    /**
     * 确保对象的最后一个属性后有逗号
     */
    trailingComma: 'es5',
    /**
     * 大括号有空格 { name: 'rose' }
     */
    bracketSpacing: true,
    /**
     * 箭头函数，单个参数添加括号
     */
    arrowParens: 'always',
    /**
     * 按照文件原样折行
     */
    proseWrap: 'preserve',
    /**
     * html文件的空格敏感度，控制空格是否影响布局
     */
    htmlWhitespaceSensitivity: 'ignore',
    /**
     * 结尾是 \n \r \n\r auto
     */
    endOfLine: 'auto',
}
