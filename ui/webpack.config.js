const path = require('path');
const webpack = require('webpack');
const vendors = require('./package.json').dependencies;
const vendorFilters = ['babel-polyfill', 'graphiql'];

module.exports = {
  devtool: 'eval',
  entry: {
    app: ['./src/app/index'],
    graphiql: ['./src/graphiql/index'],
    vendor: ["babel-polyfill", "react-hot-loader/patch", ...Object.keys(vendors).filter(v => !vendorFilters.includes(v))]
  },
  output: {
    path: path.join(__dirname, '../public/javascripts/bundles'),
    filename: '[name].bundle.js',
    publicPath: 'http://localhost:3000/assets/javascripts/bundles'
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NamedModulesPlugin(),
    new webpack.ContextReplacementPlugin(
      /graphql-language-service-interface[\\/]dist$/,
      new RegExp(`^\\./.*\\.js$`)
    ),
    new webpack.optimize.CommonsChunkPlugin({name: 'vendor', filename: 'vendor.bundle.js'})
  ],
  module: {
    loaders: [{
      test: /\.js?$/,
      loaders: ['babel-loader'],
      include: path.join(__dirname, 'src')
    }]
  },
  devServer: {
    hot: true,
    port: 3000,
    proxy: {
      '**': {
        target: 'http://localhost:9000',
      }
    }
  }
};
