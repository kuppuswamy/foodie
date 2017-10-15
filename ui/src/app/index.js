import 'babel-polyfill';
import React from 'react';
import {render} from 'react-dom';
import Root from './containers/Root'
import {AppContainer} from 'react-hot-loader'

const _render = Component => {
  render(
    <AppContainer>
      <Component/>
    </AppContainer>,
    document.getElementById('container'),
  )
};

_render(Root);

// Webpack Hot Module Replacement API
if (module.hot) {
  module.hot.accept('./containers/Root', () => {
    _render(Root)
  })
}