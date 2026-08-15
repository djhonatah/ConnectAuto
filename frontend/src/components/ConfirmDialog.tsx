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

/**
 * Diálogo de confirmação genérico, baseado em <dialog> nativo: o navegador
 * já cuida de foco preso dentro do diálogo, fechar com Esc e um backdrop.
 */
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
        // Clique no backdrop (::backdrop) cai direto no <dialog>, nunca nos
        // filhos — é assim que diferenciamos "clicou fora" de "clicou dentro".
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
