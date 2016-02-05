module.exports = {
    entry: './public/index.js',
    output: {
        filename: './public/dist/browser-bundle.js'
    },
    devtool: 'source-map',
    module: {
        loaders: [
            {
                test: /\.js$/,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015', 'react']
                }
            },
        ]
    }
};