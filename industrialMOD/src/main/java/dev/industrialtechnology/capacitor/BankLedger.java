package dev.industrialtechnology.capacitor;

import java.util.*;

/** RF belongs to physical cells. Networks only aggregate and schedule access to those cells. */
public final class BankLedger {
    public static final class Cell {
        private long energy,reserved,budgetTick=Long.MIN_VALUE,inSpent,outSpent,previousTick=Long.MIN_VALUE,previousIn,previousOut;
        public final long capacity;
        private final Runnable dirty;
        public Cell(long capacity,long energy,Runnable dirty){if(capacity<1)throw new IllegalArgumentException("capacity");this.capacity=capacity;this.energy=Math.max(0,Math.min(capacity,energy));this.dirty=dirty;}
        public long energy(){return energy;}
        public void load(long energy){if(reserved!=0)throw new IllegalStateException("reserved");this.energy=Math.max(0,Math.min(capacity,energy));changed();}
        private void changed(){if(dirty!=null)dirty.run();}
        private void budget(long tick){if(budgetTick!=tick){previousTick=budgetTick;previousIn=inSpent;previousOut=outSpent;budgetTick=tick;inSpent=outSpent=0;}}
    }
    private final List<Cell> cells;
    private final long capacity,inputPhysical,outputPhysical;
    private final int inputPerCell,outputPerCell;
    private long energy,reserved,tick=Long.MIN_VALUE,inUsed,outUsed,lastIn,lastOut;
    private int fillCursor,drainCursor,inCursor,outCursor;
    public BankLedger(List<Cell> cells,int inputPerCell,int outputPerCell){
        if(cells.isEmpty()||inputPerCell<0||outputPerCell<0)throw new IllegalArgumentException();
        this.cells=Collections.unmodifiableList(new ArrayList<Cell>(cells));this.inputPerCell=inputPerCell;this.outputPerCell=outputPerCell;
        long cap=0,stored=0,held=0;for(Cell c:cells){cap=add(cap,c.capacity);stored=add(stored,c.energy);held=add(held,c.reserved);}
        capacity=cap;energy=stored;reserved=held;inputPhysical=(long)cells.size()*inputPerCell;outputPhysical=(long)cells.size()*outputPerCell;
    }
    public static long add(long a,long b){return a>Long.MAX_VALUE-b?Long.MAX_VALUE:a+b;}
    public long stored(){return energy;}
    public long capacity(){return capacity;}
    public long maxInput(){return inputPhysical;}
    public long maxOutput(){return outputPhysical;}
    public long lastInput(long now){roll(now);return lastIn;}
    public long lastOutput(long now){roll(now);return lastOut;}
    public long inputUsed(long now){roll(now);return inUsed;}
    public long outputUsed(long now){roll(now);return outUsed;}
    public void roll(long now){
        if(tick==now)return;
        if(tick!=Long.MIN_VALUE){
            lastIn=tick==now-1?inUsed:0;lastOut=tick==now-1?outUsed:0;
            inUsed=outUsed=0;tick=now;return;
        }
        // One scan on reconstruction preserves same-tick quotas through merges and splits.
        for(Cell c:cells){if(c.budgetTick==now){inUsed=add(inUsed,c.inSpent);outUsed=add(outUsed,c.outSpent);if(c.previousTick==now-1){lastIn=add(lastIn,c.previousIn);lastOut=add(lastOut,c.previousOut);}}else if(c.budgetTick==now-1){lastIn=add(lastIn,c.inSpent);lastOut=add(lastOut,c.outSpent);}}
        tick=now;
    }
    private long limit(long configured,long physical){return configured<0?physical:Math.min(configured,physical);}
    public int receive(int request,long now,long configured,boolean simulate){
        if(request<=0)return 0;roll(now);
        int amount=(int)Math.max(0,Math.min((long)request,Math.min(Math.max(0,capacity-energy-reserved),Math.max(0,limit(configured,inputPhysical)-inUsed))));
        if(simulate||amount==0)return amount;
        long left=amount;int visited=0;
        while(left>0&&visited++<cells.size()){Cell c=cells.get(fillCursor);long n=Math.min(left,Math.max(0,c.capacity-c.energy-c.reserved));if(n>0){c.energy+=n;c.changed();left-=n;}if(c.energy+c.reserved>=c.capacity)fillCursor=(fillCursor+1)%cells.size();}
        if(left!=0)throw new IllegalStateException("deposit accounting");
        spend(amount,now,true,null);inUsed+=amount;energy+=amount;return amount;
    }
    public int extract(int request,long now,long configured,boolean simulate){
        int amount=availableOutput(request,now,configured);if(simulate||amount==0)return amount;
        drain(amount,false,null);spend(amount,now,false,null);outUsed+=amount;energy-=amount;return amount;
    }
    private int availableOutput(int request,long now,long configured){if(request<=0)return 0;roll(now);return (int)Math.max(0,Math.min((long)request,Math.min(energy,Math.max(0,limit(configured,outputPhysical)-outUsed))));}
    private void drain(int amount,boolean reserve,List<Step> debits){
        long left=amount;int visited=0;
        while(left>0&&visited++<cells.size()){Cell c=cells.get(drainCursor);long n=Math.min(left,c.energy);if(n>0){c.energy-=n;if(reserve){c.reserved+=n;debits.add(new Step(c,n));}c.changed();left-=n;}if(c.energy==0)drainCursor=(drainCursor+1)%cells.size();}
        if(left!=0)throw new IllegalStateException("withdrawal accounting");
    }
    private void spend(long amount,long now,boolean input,List<Step> records){
        long left=amount;int i=input?inCursor:outCursor,visited=0,rate=input?inputPerCell:outputPerCell;
        while(left>0&&visited++<cells.size()){Cell c=cells.get(i);c.budget(now);long used=input?c.inSpent:c.outSpent,n=Math.min(left,Math.max(0,rate-used));if(n>0){if(input)c.inSpent+=n;else c.outSpent+=n;if(records!=null)records.add(new Step(c,n));left-=n;}if((input?c.inSpent:c.outSpent)>=rate)i=(i+1)%cells.size();}
        if(left!=0)throw new IllegalStateException("rate accounting");if(input)inCursor=i;else outCursor=i;
    }
    private static final class Step {final Cell cell;final long amount;Step(Cell cell,long amount){this.cell=cell;this.amount=amount;}}
    /** Debit before invoking another mod; rejected RF returns to its original owners and budget. */
    public Reservation reserveOutput(int request,long now,long configured){
        int n=availableOutput(request,now,configured);Reservation r=new Reservation(n,now);if(n==0)return r;
        drain(n,true,r.debits);spend(n,now,false,r.quotas);energy-=n;reserved+=n;outUsed+=n;return r;
    }
    public final class Reservation {
        public final int amount;private final long epoch;private boolean settled;
        private final List<Step> debits=new ArrayList<Step>(),quotas=new ArrayList<Step>();
        private Reservation(int amount,long epoch){this.amount=amount;this.epoch=epoch;}
        public int settle(int accepted){
            if(settled)throw new IllegalStateException("already settled");settled=true;accepted=Math.max(0,Math.min(amount,accepted));long refund=(long)amount-accepted,left=refund;
            for(int i=debits.size()-1;i>=0;i--){Step s=debits.get(i);s.cell.reserved-=s.amount;long n=Math.min(left,s.amount);s.cell.energy+=n;left-=n;s.cell.changed();}
            left=refund;for(int i=quotas.size()-1;i>=0;i--){Step s=quotas.get(i);long n=Math.min(left,s.amount);if(s.cell.budgetTick==epoch)s.cell.outSpent-=n;left-=n;}
            reserved-=amount;energy+=refund;if(tick==epoch)outUsed-=refund;return accepted;
        }
    }
}
