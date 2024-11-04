package com.example.validate;


import javax.validation.GroupSequence;
import javax.validation.groups.Default;

@GroupSequence(value = {Username.class, Password.class})
public interface EditGroup extends Default {
}
