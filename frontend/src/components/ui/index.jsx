import React,{useEffect,useRef} from 'react';

export function Button({children,variant='primary',className='',type='button',...props}){
 return <button type={type} className={['ui-button',variant,className].filter(Boolean).join(' ')} {...props}>{children}</button>;
}

export function Card({children,className='',as:Element='section',...props}){
 return <Element className={['ui-card',className].filter(Boolean).join(' ')} {...props}>{children}</Element>;
}

export function Badge({children,tone='neutral',className='',...props}){
 return <span className={['ui-badge','tone-'+tone,className].filter(Boolean).join(' ')} {...props}>{children}</span>;
}

export function DataTable({columns,rows,rowKey='id',loading=false,error='',emptyLabel='No records found.'}){
 if(loading)return <Skeleton/>;
 if(error)return <p className="error" role="alert">{error}</p>;
 if(!rows?.length)return <p className="muted" role="status">{emptyLabel}</p>;
 return <div className="ui-table-scroll"><table className="ui-data-table">
  <thead><tr>{columns.map(column=><th key={column.key} scope="col">{column.label}</th>)}</tr></thead>
  <tbody>{rows.map((row,index)=><tr key={row[rowKey]??index}>{columns.map(column=><td key={column.key}>{column.render?column.render(row):row[column.key]}</td>)}</tr>)}</tbody>
 </table></div>;
}

export function Modal({open,title,children,onClose,actions}){
 const closeRef=useRef(null);
 useEffect(()=>{
  if(!open)return undefined;
  closeRef.current?.focus();
  const handleKey=event=>{if(event.key==='Escape')onClose?.();};
  window.addEventListener('keydown',handleKey);
  return ()=>window.removeEventListener('keydown',handleKey);
 },[open,onClose]);
 if(!open)return null;
 return <div className="ui-modal-backdrop" onMouseDown={event=>{if(event.target===event.currentTarget)onClose?.();}}>
  <section className="ui-modal" role="dialog" aria-modal="true" aria-labelledby="ui-modal-title">
   <header><h2 id="ui-modal-title">{title}</h2><button ref={closeRef} type="button" className="text-link" onClick={onClose} aria-label="Close dialog">×</button></header>
   <div>{children}</div>
   {actions&&<footer>{actions}</footer>}
  </section>
 </div>;
}

export function Toast({message,tone='info',onDismiss}){
 if(!message)return null;
 return <div className={'ui-toast tone-'+tone} role={tone==='error'?'alert':'status'} aria-live="polite">
  <span>{message}</span>{onDismiss&&<button type="button" onClick={onDismiss} aria-label="Dismiss notification">×</button>}
 </div>;
}

export function ConfirmDialog({open,title,message,confirmLabel='Confirm',cancelLabel='Cancel',onConfirm,onCancel,danger=false}){
 return <Modal open={open} title={title} onClose={onCancel} actions={<><Button variant="secondary" onClick={onCancel}>{cancelLabel}</Button><Button variant={danger?'danger':'primary'} onClick={onConfirm}>{confirmLabel}</Button></>}>
  <p>{message}</p>
 </Modal>;
}

export function FormField({label,htmlFor,error,hint,children}){
 return <div className="ui-form-field">
  <label htmlFor={htmlFor}>{label}</label>
  {children}
  {hint&&<small>{hint}</small>}
  {error&&<small className="ui-field-error" role="alert">{error}</small>}
 </div>;
}

export function Skeleton({rows=3}){
 return <div className="ui-skeleton-list" role="status" aria-label="Loading">
  {Array.from({length:rows},(_,index)=><span key={index}/>)}
 </div>;
}

export function PageHeader({eyebrow,title,description,action}){
 return <header className="admin-page-header">
  <div>{eyebrow&&<span className="eyebrow">{eyebrow}</span>}<h1>{title}</h1>{description&&<p className="muted">{description}</p>}</div>
  {action}
 </header>;
}
