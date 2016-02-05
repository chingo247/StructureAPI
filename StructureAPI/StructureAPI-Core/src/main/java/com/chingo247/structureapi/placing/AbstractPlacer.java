/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Chingo
 * @param <T> The type that extends this type...
 */
public abstract class AbstractPlacer<T> {
    
    private Set<UUID> owners;
    private Set<UUID> masters;
    private Set<UUID> members;

    public AbstractPlacer() {
        this.owners = Sets.newHashSet();
        this.masters = Sets.newHashSet();
        this.members = Sets.newHashSet();
    }

    public Set<UUID> getMasters() {
        return masters;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getOwners() {
        return owners;
    }
    
    public T addMaster(UUID player) {
        this.masters.add(player);
        return (T) this;
    }

    public T addMasters(Set<UUID> players) {
        if(this.masters.isEmpty()) {
            this.masters = players;
        } else {
            this.masters.addAll(players);
        }
        return (T) this;
    }
    
    public T addMember(UUID member) {
        this.members.add(member);
        return (T) this;
    }

    public T addMembers(Set<UUID> members) {
        if (this.members.isEmpty()) {
            this.members = members;
        } else {
            this.members.addAll(members);
        }
        return (T) this;
    }

    public T addOwner(UUID owner) {
        this.owners.add(owner);
        return (T) this;
    }

    public T addOwners(Set<UUID> owners) {
        if (this.owners.isEmpty()) {
            this.owners = owners;
        } else {
            this.owners.addAll(owners);
        }
        return (T) this;
    }

   
    
    
}
