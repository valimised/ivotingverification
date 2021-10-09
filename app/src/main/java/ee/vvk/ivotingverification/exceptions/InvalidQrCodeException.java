package ee.vvk.ivotingverification.exceptions;

import ee.vvk.ivotingverification.util.C;

public class InvalidQrCodeException extends VerifierException {

    public InvalidQrCodeException() {
        super(C.problemQrCodeMessage);
    }
}
