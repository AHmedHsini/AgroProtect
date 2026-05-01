export enum StatusNotification {
    ENVOYE = 'ENVOYE',
    LU = 'LU',
    NON_LU = 'NON_LU',
    ERREUR = 'ERREUR'
  }
  
  export interface NotificationHistory {
    id?: number;
    to: number;              // recipient_id
    from?: number;           // sender_id (optional)
    subject: string;
    content: string;
    sentDate?: string;       // ISO string from backend
    status: StatusNotification;
    templateId?: number;
  }