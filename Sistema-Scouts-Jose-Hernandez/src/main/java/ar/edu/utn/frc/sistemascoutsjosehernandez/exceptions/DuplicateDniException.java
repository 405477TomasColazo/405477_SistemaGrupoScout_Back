package ar.edu.utn.frc.sistemascoutsjosehernandez.exceptions;

public class DuplicateDniException extends Exception {
    private Integer memberId;
    
    public DuplicateDniException(String message, Integer memberId) {
        super(message);
        this.memberId = memberId;
    }
    
    public Integer getMemberId() {
        return memberId;
    }
}