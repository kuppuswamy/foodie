import React from 'react';
import AddFoodMutation from '../mutations/AddFoodMutation';
import EditFoodMutation from '../mutations/EditFoodMutation';
import DeleteFoodMutation from '../mutations/DeleteFoodMutation';
import {COUNT} from '../containers/Foods';

export default class Foods extends React.Component {
  state = {
    text: '',
    editID: null,
    typeID: null,
    showModal: false,
    sort: 'desc',
    loadingSort: false,
    hasPreviousPage: false,
    hasNextPage: false,
    loadingNextPage: false,
    loadingPreviousPage: false
  };
  _getStore = () => this.props.relay.environment.getStore().getSource();
  _handleTextChange = (e) => {
    this.setState({text: e.target.value});
  };
  _handleTypeChange = (e) => {
    this.setState({typeID: e.target.value ? e.target.value : null});
  };
  _onAdd = () => {
    if (this.state.text.trim() !== '') {
      AddFoodMutation.commit(this.props.relay.environment, this.state.text, this.state.typeID);
    }
    this.setState({text: '', typeID: null, showModal: false});
  };
  _onEdit = (food) => {
    this.setState({
      text: food.name,
      editID: food.id,
      typeID: food.type.id,
      showModal: true
    }, () => this.foodName.focus());
  };
  _onCancel = () => {
    this.setState({text: '', editID: null, typeID: null, showModal: false});
  };
  _onSave = () => {
    if (this.state.typeID === null) return;
    let food = {name: this.state.text, id: this.state.editID};
    this.setState({text: '', editID: null, typeID: null, showModal: false});
    let store = this._getStore();
    EditFoodMutation.commit(this.props.relay.environment, food, store.get(this.state.typeID));
  };
  _onDelete = (food) => {
    DeleteFoodMutation.commit(this.props.relay.environment, food);
  };
  _showModal = () => {
    this.setState({showModal: true}, () => this.foodName.focus());
  };
  _onEnter = e => {
    if (e.keyCode === 13) {
      if (this.state.editID) this._onSave(); else this._onAdd();
    }
  };
  _getNextSort = sort => (sort === 'desc' ? 'asc' : 'desc');
  _sort = sort => {
    this.setState({loadingSort: true});
    const refetchVariables = fragmentVariables => ({
      sort: sort,
      first: COUNT,
      after: null,
      last: null,
      before: null
    });
    this.props.relay.refetch(refetchVariables, null, error => {
      if (!error) this.setState({sort: this._getNextSort(this.state.sort), loadingSort: false});
      else this.setState({loadingSort: true});
    });
  };
  _loadNext = (endCursor) => {
    this.setState({loadingNextPage: true});
    const refetchVariables = fragmentVariables => ({
      first: COUNT,
      after: endCursor,
      last: null,
      before: null,
      sort: this.state.sort
    });
    this.props.relay.refetch(refetchVariables, null, error => {
      if (!error) this.setState({
        hasNextPage: this.props.foodStore.foods.pageInfo.hasNextPage,
        hasPreviousPage: true,
        loadingNextPage: false
      });
      else this.setState({loadingNextPage: false});
    });
  };
  _loadPrevious = (startCursor) => {
    this.setState({loadingPreviousPage: true});
    const refetchVariables = fragmentVariables => ({
      first: null,
      after: null,
      last: COUNT,
      before: startCursor,
      sort: this.state.sort
    });
    this.props.relay.refetch(refetchVariables, null, error => {
      if (!error) this.setState({
        hasPreviousPage: this.props.foodStore.foods.pageInfo.hasPreviousPage,
        hasNextPage: true,
        loadingPreviousPage: false
      });
      else this.setState({loadingPreviousPage: false});
    });
  };
  render = () => {
    let {foodStore, typeStore} = this.props;
    let {foods} = foodStore;
    let types = typeStore.typesForAddFood;
    return (
      <div>
        <div className={`modal ${this.state.showModal ? 'is-active' : ''}`}>
          <div className="modal-background" onClick={e => this._onCancel()}/>
          <div className="modal-card">
            <header className="modal-card-head">
              <p className="modal-card-title">{this.state.editID ? 'Edit' : 'Add'} food</p>
              <button className="delete" aria-label="close" onClick={e => this._onCancel()}/>
            </header>
            <section className="modal-card-body">
              <div className="field">
                <p className="control is-expanded">
                  <input ref={n => this.foodName = n} className="input" type="text" placeholder="Name"
                         value={this.state.text} onChange={this._handleTextChange} onKeyDown={e => this._onEnter(e)}/>
                </p>
              </div>
              <div className="field">
                <div className="control is-expanded">
                  <div className="select is-fullwidth">
                    <select value={this.state.typeID ? this.state.typeID : ''} onChange={this._handleTypeChange}>
                      <option value={''}>Select a type</option>
                      {
                        types.edges.map((type, key) => (
                          <option key={type.node.id} value={type.node.id}>{type.node.name}</option>
                        ))
                      }
                    </select>
                  </div>
                </div>
              </div>
            </section>
            <footer className="modal-card-foot">
              {
                this.state.editID ? (
                  <button className="button is-danger" onClick={e => this._onSave()}>Save</button>
                ) : (
                  <button className="button is-danger" onClick={e => this._onAdd()}>Add</button>
                )
              }
              <button className="button" onClick={e => this._onCancel()}>Cancel</button>
            </footer>
          </div>
        </div>
        <h1 className="title">Foods</h1>
        <div className="field is-grouped">
          <p className="control">
            <a className="button is-warning" onClick={e => this._showModal()}>Add</a>
          </p>
          <p className="control">
            <a className={`button is-capitalized${this.state.loadingSort ? ' is-loading' : ''}`}
               onClick={e => this._sort(this._getNextSort(this.state.sort))}>{this.state.sort === 'desc' ? 'latest' : 'oldest'}</a>
          </p>
        </div>
        {
          foods.edges.length ?
            foods.edges.map((food, key) => (
              <div className="field card" key={food.node.id}>
                <div className="card-content">
                  <p className="subtitle is-4">
                    {food.node.name}{' - #'}{food.node.id}
                  </p>
                  <p className="subtitle is-6">
                    {food.node.type.name}{' - #'}{food.node.type.id}
                  </p>
                </div>
                <footer className="card-footer">
                  <a className="card-footer-item" onClick={e => this._onEdit(food.node)}>Edit</a>
                  <a className="card-footer-item" onClick={e => this._onDelete(food.node)}>Delete</a>
                </footer>
              </div>
            )) : (
              <div className="field card has-text-centered">
                <div className="card-content">
                  <p className="subtitle is-4">
                    <i>Add a food<br/>with it's type.</i>
                  </p>
                </div>
              </div>
            )
        }
        <div className="field is-grouped">
          <p className="control">
            <button onClick={e => this._loadPrevious(foods.pageInfo.startCursor)}
                    className={`button${this.state.loadingPreviousPage ? ' is-loading' : ''}`}
                    disabled={!foods.pageInfo.hasPreviousPage && !this.state.hasPreviousPage}>
              Previous
            </button>
          </p>
          <p className="control">
            <button onClick={e => this._loadNext(foods.pageInfo.endCursor)}
                    className={`button${this.state.loadingNextPage ? ' is-loading' : ''}`}
                    disabled={!foods.pageInfo.hasNextPage && !this.state.hasNextPage}>
              Next
            </button>
          </p>
        </div>
      </div>
    );
  }
}