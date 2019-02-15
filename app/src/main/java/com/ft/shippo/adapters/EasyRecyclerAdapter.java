package com.ft.shippo.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ft.shippo.R;
import com.ft.shippo.holders.LoadingHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class EasyRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    protected int mModelLayout;
    Class<T> mModelClass;
    List<T> itens = new ArrayList<>();
    Class<VH> mViewHolderClass;

    boolean isLoading = false;
    public EasyRecyclerAdapter(List<T> itens, Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass) {
        mModelClass = modelClass;
        mModelLayout = modelLayout;
        mViewHolderClass = viewHolderClass;
        this.itens = itens;
    }


    public void cleanup() {
        itens.clear();
    }

    public void feedMore(List<T> moreItems) {
        itens.addAll(moreItems);
        notifyItemRangeInserted(itens.size() - moreItems.size() - 1, itens.size() - 1);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public T getItem(int position) {
        return itens.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType != -1){
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        view.setTag(viewType);
        try {
            Constructor<VH> constructor = mViewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
            }
        } else{
            return new LoadingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_holder, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(itens.get(position) != null) {
            T model = getItem(position);
            populateViewHolder((VH) viewHolder, model, position);
        }
    }

    public void setLoading(){
        isLoading = true;
        itens.add(null);
        notifyItemInserted(itens.size()-1);
    }

    public void stopLoading(){
        isLoading = false;
        itens.remove(null);
        notifyItemRemoved(itens.size());
    }
    @Override
    public int getItemViewType(int position) {
        return (itens.get(position) != null)? mModelLayout : -1;
    }

    abstract protected void populateViewHolder(VH viewHolder, T model, int position);

    public boolean isLoading() {
        return isLoading;
    }
}
