package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.persistence.Model;

/**
 *
 * @author Faithcaio
 */
public abstract class AccountModel implements IAccount,Model
{
    private double balance;
    private int id;
    
    public abstract String getName();

    public double give(double amount)
    {
        return (this.balance += amount);
    }

    public double take(double amount)
    {
        return (this.balance -= amount);
    }

    public double deposit(IAccount acc, double amount)
    {
        acc.take(amount);
        return this.give(amount);
    }

    public double withdraw(IAccount acc, double amount)
    {
        acc.give(amount);
        return this.take(amount);
    }

    public double balance()
    {
        return this.balance;
    }

    public void reset()
    {
        this.balance = 0;
    }

    public void set(double amount)
    {
        this.balance = amount;
    }

    public double scale(double factor)
    {
        return (this.balance *= factor);
    }
    
    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
