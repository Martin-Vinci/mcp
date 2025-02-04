export class MessageBox {
    messageId: number;
    messageText: String;
    timeGenerated: Date;
    timeSent: Date;
    recipientNumber: string;
    messageStatus: string;
    flashMessage: boolean = false;
    emailMessage: boolean = false;
    emailSubject: string;
    failureReason: string;
    emailAttachment: string;
}
