/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.owner;

import com.chingo247.structureapi.model.owner.OwnerType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Chingo
 * @param <T> The type that extends this type...
 */
public class OwnerDomain {

    private Set<UUID> owners;
    private Set<UUID> masters;
    private Set<UUID> members;

    public OwnerDomain() {
        this.owners = Sets.newHashSet();
        this.masters = Sets.newHashSet();
        this.members = Sets.newHashSet();
    }

    public Set<UUID> getOwners() {
        return getOwners(null);
    }

    public Set<UUID> getOwners(OwnerType type) {
        Set<UUID> ownerSet;

        switch (type) {
            case MEMBER:
                ownerSet = new HashSet<>(members);
                break;
            case OWNER:
                ownerSet = new HashSet<>(owners);
                break;
            case MASTER:
                ownerSet = new HashSet<>(masters);
                break;
            default:
                ownerSet = new HashSet<>();
                ownerSet.addAll(owners);
                ownerSet.addAll(members);
                ownerSet.addAll(masters);
                break;
        }
        return ownerSet;
    }

    public void addOwner(UUID owner, OwnerType type) {
        Preconditions.checkNotNull(type, "Type was null");
        Preconditions.checkNotNull(owner, "Owner was null");
        switch (type) {
            case MEMBER:
                masters.remove(owner);
                owners.remove(owner);
                members.add(owner);
                break;
            case OWNER:
                masters.remove(owner);
                members.remove(owner);
                owners.add(owner);
                break;
            case MASTER:
                members.remove(owner);
                owners.remove(owner);
                masters.add(owner);
                break;
            default:
                break;
        }
    }

    public void removeOwner(UUID owner) {
        Preconditions.checkNotNull(owner, "Owner was null");
        masters.remove(owner);
        owners.remove(owner);
        members.remove(owner);
    }

}
