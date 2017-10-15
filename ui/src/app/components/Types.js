import React from 'react';
import AddTypeMutation from '../mutations/AddTypeMutation';
import EditTypeMutation from '../mutations/EditTypeMutation';
import DeleteTypeMutation from '../mutations/DeleteTypeMutation';

export default class Types extends React.Component {
  state = {
    text: '',
    editID: null,
    showModal: false
  };
  _handleChange = (e) => {
    this.setState({text: e.target.value});
  };
  _onAdd = () => {
    if (this.state.text.trim() !== '') {
      AddTypeMutation.commit(this.props.relay.environment, this.state.text);
    }
    this.setState({text: '', showModal: false});
  };
  _onEdit = (type) => {
    this.setState({text: type.name, editID: type.id, showModal: true}, () => this.typeName.focus());
  };
  _onCancel = () => {
    this.setState({text: '', editID: null, showModal: false});
  };
  _onSave = () => {
    let type = {name: this.state.text, id: this.state.editID};
    this.setState({text: '', editID: null, showModal: false});
    EditTypeMutation.commit(this.props.relay.environment, type);
  };
  _onDelete = (type) => {
    DeleteTypeMutation.commit(this.props.relay.environment, type);
  };
  _showModal = () => {
    this.setState({showModal: true}, () => this.typeName.focus());
  };
  _onEnter = e => {
    if (e.keyCode === 13) {
      if (this.state.editID) this._onSave(); else this._onAdd();
    }
  };
  render = () => {
    let {types} = this.props;
    return (
      <div>
        <div className={`modal ${this.state.showModal ? 'is-active' : ''}`}>
          <div className="modal-background" onClick={e => this._onCancel()}/>
          <div className="modal-card">
            <header className="modal-card-head">
              <p className="modal-card-title">{this.state.editID ? 'Edit' : 'Add'} type</p>
              <button className="delete" aria-label="close" onClick={e => this._onCancel()}/>
            </header>
            <section className="modal-card-body">
              <div className="field">
                <p className="control is-expanded">
                  <input ref={n => this.typeName = n} className="input" type="text" placeholder="Name"
                         value={this.state.text} onChange={this._handleChange} onKeyDown={e => this._onEnter(e)}/>
                </p>
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
        <h1 className="title">Types</h1>
        <div className="field">
          <p className="control">
            <a className="button is-warning" onClick={e => this._showModal()}>Add</a>
          </p>
        </div>
        {
          types.edges.length ?
            types.edges.map((type, key) => (
              <div className="field card" key={key}>
                <div className="card-content">
                  <p className="subtitle is-4">
                    {type.node.name}{' - #'}{type.node.id}
                  </p>
                </div>
                <footer className="card-footer">
                  <a className="card-footer-item" onClick={e => this._onEdit(type.node)}>Edit</a>
                  <a className="card-footer-item" onClick={e => this._onDelete(type.node)}>Delete</a>
                </footer>
              </div>
            )) : (
              <div className="field card has-text-centered">
                <div className="card-content">
                  <p className="subtitle is-4">
                    <i>Add a type<br/>of food.</i>
                  </p>
                </div>
              </div>
            )
        }
      </div>
    );
  }
}