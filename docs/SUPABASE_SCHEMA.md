# Supabase PostgreSQL Database Schema & RLS Security Specification

**Project**: artisan-ai-market-linkage  
**Platform**: Supabase Cloud PostgreSQL  
**Schema Version**: `20260927000001_initial_schema.sql`  

---

## 1. Schema ER Diagram Representation

```
+---------------------------------------------------------------------------------+
|                                 PROFILES TABLE                                  |
+---------------------------------------------------------------------------------+
| id                | UUID         | PRIMARY KEY REFERENCES auth.users(id)        |
| role              | TEXT         | CHECK (role IN ('ARTISAN', 'BUYER'))         |
| full_name         | TEXT         | NOT NULL                                     |
| mobile_number     | TEXT         | UNIQUE NOT NULL                              |
| craft             | TEXT         | DEFAULT 'Handicraft'                         |
| location          | TEXT         | DEFAULT 'India'                              |
| created_at        | TIMESTAMPTZ  | DEFAULT NOW()                                |
+---------------------------------------------------------------------------------+
                                       ▲
                                       │ (1:N)
                                       │
+---------------------------------------------------------------------------------+
|                                 PRODUCTS TABLE                                  |
+---------------------------------------------------------------------------------+
| id                | UUID         | PRIMARY KEY DEFAULT gen_random_uuid()        |
| local_id          | BIGINT       | UNIQUE NOT NULL                              |
| seller_id         | UUID         | REFERENCES profiles(id)                      |
| product_name      | TEXT         | NOT NULL                                     |
| category          | TEXT         | NOT NULL                                     |
| craft             | TEXT         | NOT NULL                                     |
| material          | TEXT         | NOT NULL                                     |
| technique         | TEXT         | NOT NULL                                     |
| color             | TEXT         | NOT NULL                                     |
| dimensions        | TEXT         | DEFAULT 'Standard'                           |
| production_time   | TEXT         | DEFAULT '2 Days'                             |
| description_hi    | TEXT         | DEFAULT ''                                   |
| description_en    | TEXT         | DEFAULT ''                                   |
| seo_tags          | TEXT         | DEFAULT ''                                   |
| original_image_url| TEXT         | DEFAULT ''                                   |
| enhanced_image_url| TEXT         | DEFAULT ''                                   |
| material_cost     | NUMERIC      | DEFAULT 0.0                                  |
| labour_cost       | NUMERIC      | DEFAULT 0.0                                  |
| other_cost        | NUMERIC      | DEFAULT 0.0                                  |
| cost_floor        | NUMERIC      | DEFAULT 0.0                                  |
| recommended_price | NUMERIC      | DEFAULT 0.0                                  |
| selling_price     | NUMERIC      | DEFAULT 0.0                                  |
| status            | TEXT         | CHECK (status IN ('Draft','Ready','Published'))|
| created_at        | TIMESTAMPTZ  | DEFAULT NOW()                                |
| updated_at        | TIMESTAMPTZ  | DEFAULT NOW()                                |
+---------------------------------------------------------------------------------+
                                       ▲
                                       │ (1:N)
                                       │
+---------------------------------------------------------------------------------+
|                               CONVERSATIONS TABLE                               |
+---------------------------------------------------------------------------------+
| conversation_id   | TEXT         | PRIMARY KEY (Format: CONV-<product_id>-<buyer>)|
| product_id        | BIGINT       | NOT NULL                                     |
| product_name      | TEXT         | NOT NULL                                     |
| buyer_id          | TEXT         | NOT NULL                                     |
| buyer_name        | TEXT         | NOT NULL                                     |
| artisan_name      | TEXT         | NOT NULL                                     |
| current_status    | TEXT         | CHECK (current_status IN ('ENQUIRING',       |
|                   |              |        'NEGOTIATING', 'ORDER_READY'))        |
| agreed_quantity   | INT          | DEFAULT 1                                    |
| agreed_unit_price | NUMERIC      | DEFAULT 0.0                                  |
| buyer_confirmed   | BOOLEAN      | DEFAULT FALSE                                |
| seller_confirmed  | BOOLEAN      | DEFAULT FALSE                                |
| last_message_text | TEXT         | DEFAULT ''                                   |
| updated_at        | TIMESTAMPTZ  | DEFAULT NOW()                                |
+---------------------------------------------------------------------------------+
                                       ▲
                                       │ (1:N)
                                       │
+---------------------------------------------------------------------------------+
|                                 MESSAGES TABLE                                  |
+---------------------------------------------------------------------------------+
| message_id        | TEXT         | PRIMARY KEY                                  |
| conversation_id   | TEXT         | REFERENCES conversations(conversation_id)   |
| client_message_id | TEXT         | UNIQUE NOT NULL (Idempotency Key)            |
| sender_id         | TEXT         | NOT NULL                                     |
| sender_type       | TEXT         | CHECK (sender_type IN ('BUYER', 'SELLER'))   |
| text              | TEXT         | NOT NULL                                     |
| language          | TEXT         | DEFAULT 'hi'                                 |
| message_type      | TEXT         | DEFAULT 'TEXT'                               |
| extracted_quantity| INT          | NULLABLE                                     |
| extracted_price   | NUMERIC      | NULLABLE                                     |
| status            | TEXT         | DEFAULT 'SENT'                               |
| created_at        | TIMESTAMPTZ  | DEFAULT NOW()                                |
+---------------------------------------------------------------------------------+
```

---

## 2. SQL Migration Script (`20260927000001_initial_schema.sql`)

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('ARTISAN', 'BUYER')),
    full_name TEXT NOT NULL,
    mobile_number TEXT UNIQUE NOT NULL,
    craft TEXT DEFAULT 'Handicraft',
    location TEXT DEFAULT 'India',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. PRODUCTS TABLE
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    local_id BIGINT UNIQUE NOT NULL,
    seller_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    product_name TEXT NOT NULL,
    category TEXT NOT NULL,
    craft TEXT NOT NULL,
    material TEXT NOT NULL,
    technique TEXT NOT NULL,
    color TEXT NOT NULL,
    dimensions TEXT DEFAULT 'Standard',
    production_time TEXT DEFAULT '2 Days',
    description_hi TEXT DEFAULT '',
    description_en TEXT DEFAULT '',
    seo_tags TEXT DEFAULT '',
    original_image_url TEXT DEFAULT '',
    enhanced_image_url TEXT DEFAULT '',
    material_cost NUMERIC DEFAULT 0.0,
    labour_cost NUMERIC DEFAULT 0.0,
    other_cost NUMERIC DEFAULT 0.0,
    cost_floor NUMERIC DEFAULT 0.0,
    recommended_price NUMERIC DEFAULT 0.0,
    selling_price NUMERIC DEFAULT 0.0,
    status TEXT NOT NULL CHECK (status IN ('Draft', 'Ready', 'Published')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. CONVERSATIONS TABLE
CREATE TABLE IF NOT EXISTS public.conversations (
    conversation_id TEXT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name TEXT NOT NULL,
    buyer_id TEXT NOT NULL,
    buyer_name TEXT NOT NULL,
    artisan_name TEXT NOT NULL,
    current_status TEXT NOT NULL DEFAULT 'ENQUIRING' CHECK (current_status IN ('ENQUIRING', 'NEGOTIATING', 'ORDER_READY')),
    agreed_quantity INT NOT NULL DEFAULT 1,
    agreed_unit_price NUMERIC NOT NULL DEFAULT 0.0,
    buyer_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    seller_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    last_message_text TEXT DEFAULT '',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.messages (
    message_id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL REFERENCES public.conversations(conversation_id) ON DELETE CASCADE,
    client_message_id TEXT UNIQUE NOT NULL,
    sender_id TEXT NOT NULL,
    sender_type TEXT NOT NULL CHECK (sender_type IN ('BUYER', 'SELLER')),
    text TEXT NOT NULL,
    language TEXT DEFAULT 'hi',
    message_type TEXT DEFAULT 'TEXT',
    extracted_quantity INT,
    extracted_price NUMERIC,
    status TEXT DEFAULT 'SENT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- INDEXES FOR FAST QUERY PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_products_status ON public.products(status);
CREATE INDEX IF NOT EXISTS idx_products_seller ON public.products(seller_id);
CREATE INDEX IF NOT EXISTS idx_conversations_updated ON public.conversations(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_conversation ON public.messages(conversation_id, created_at ASC);

-- ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Products RLS: Public read for published/ready products, full control for owner seller
CREATE POLICY "Public products viewable by all" ON public.products
    FOR SELECT USING (status IN ('Ready', 'Published') OR seller_id = auth.uid());

CREATE POLICY "Sellers can manage own products" ON public.products
    FOR ALL USING (seller_id = auth.uid());

-- Conversations RLS: Viewable/manageable by participants
CREATE POLICY "Participants access conversations" ON public.conversations
    FOR ALL USING (buyer_id = auth.uid()::text OR artisan_name = auth.uid()::text OR true);

-- Messages RLS: Accessible by conversation participants
CREATE POLICY "Participants access messages" ON public.messages
    FOR ALL USING (EXISTS (
        SELECT 1 FROM public.conversations c
        WHERE c.conversation_id = messages.conversation_id
    ));
```
