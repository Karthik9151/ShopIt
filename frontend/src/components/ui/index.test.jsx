import React from 'react';
import {afterEach,describe,expect,it,vi} from 'vitest';
import {cleanup,fireEvent,render,screen} from '@testing-library/react';
import {Badge,ConfirmDialog,DataTable,PageHeader,Toast} from './index';

describe('shared UI primitives',()=>{
 afterEach(()=>cleanup());

 it('exposes badge meaning with text, not colour alone',()=>{
  render(<Badge tone="success">Paid</Badge>);
  expect(screen.getByText('Paid').className).toContain('tone-success');
 });

 it('renders accessible table headings and cell values',()=>{
  render(<DataTable columns={[{key:'name',label:'Product'}]} rows={[{id:1,name:'Tea'}]}/>);
  expect(screen.getByRole('columnheader',{name:'Product'})).toBeTruthy();
  expect(screen.getByRole('cell',{name:'Tea'})).toBeTruthy();
 });

 it('shows empty and loading states',()=>{
  const {rerender}=render(<DataTable columns={[]} rows={[]}/>);
  expect(screen.getByRole('status').textContent).toContain('No records');
  rerender(<DataTable columns={[]} rows={[]} loading/>);
  expect(screen.getByRole('status').getAttribute('aria-label')).toBe('Loading');
 });

 it('renders a page heading and toast status',()=>{
  render(<><PageHeader eyebrow="Operations" title="Orders"/><Toast message="Saved"/> </>);
  expect(screen.getByRole('heading',{name:'Orders'})).toBeTruthy();
  expect(screen.getByRole('status').textContent).toContain('Saved');
 });

 it('confirms destructive actions from an accessible dialog',()=>{
  const confirm=vi.fn(),cancel=vi.fn();
  render(<ConfirmDialog open title="Deactivate" message="This hides the item." confirmLabel="Deactivate" onConfirm={confirm} onCancel={cancel} danger/>);
  expect(screen.getByRole('dialog',{name:'Deactivate'})).toBeTruthy();
  fireEvent.click(screen.getByRole('button',{name:'Deactivate'}));
  expect(confirm).toHaveBeenCalledOnce();
 });
});
