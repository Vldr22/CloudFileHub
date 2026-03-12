package org.resume.s3filemanager.exception;

import org.resume.common.model.ScanStatus;
import org.resume.s3filemanager.constant.ErrorMessages;

public class InvalidScanStatusException extends RuntimeException {
  public InvalidScanStatusException(ScanStatus status) {
    super(String.format(ErrorMessages.DLT_RETRY_FAILED, status));
  }
}
