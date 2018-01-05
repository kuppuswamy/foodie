const path = require('path');
const webpack = require('webpack');
const vendors = require('./package.json').dependencies;
const vendorFilters = ['graphiql'];

module.exports = {
  entry: {
    app: ['./src/app/index'],
    graphiql: ['./src/graphiql/index'],
    vendor: Object.keys(vendors).filter(v => !vendorFilters.includes(v))
  },
  output: {
    path: path.join(__dirname, '../public/javascripts/bundles'),
    filename: '[name].bundle.js'
  },
  plugins: [
    new webpack.optimize.OccurrenceOrderPlugin(),
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      },
      comments: false,
      mangle: false
    }),
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
  }
};