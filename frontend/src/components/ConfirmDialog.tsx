import { useEffect, useRef } from 'react';
import './ConfirmDialog.css';

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  isConfirming?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  isConfirming = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    if (open && !dialog.open) {
      dialog.showModal();
    } else if (!open && dialog.open) {
      dialog.close();
    }
  }, [open]);

  return (
    <dialog
      ref={dialogRef}
      className="confirm-dialog"
      onCancel={onCancel}
      onClick={(event) => {
        if (event.target === dialogRef.current) onCancel();
      }}
    >
      <h2>{title}</h2>
      {description && <p>{description}</p>}
      <div className="confirm-dialog__actions">
        <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={isConfirming}>
          {cancelLabel}
        </button>
        <button
          type="button"
          className="btn btn-danger"
          onClick={onConfirm}
          disabled={isConfirming}
        >
          {isConfirming ? 'Excluindo…' : confirmLabel}
        </button>
      </div>
    </dialog>
  );
}
